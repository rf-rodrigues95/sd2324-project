package tukano.impl.java.servers;

import static java.lang.String.format;
import static tukano.api.java.Result.error;
import static tukano.api.java.Result.errorOrValue;
import static tukano.api.java.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static utils.DB.getOne;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import tukano.kafka.lib.RecordProcessor;
import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.impl.api.java.ExtendedShorts;
import tukano.impl.api.java.ExtendedShortsRep;
import tukano.impl.discovery.Discovery;
import tukano.impl.grpc.clients.GrpcShortsClient;
import tukano.impl.java.clients.ClientFactory;
import tukano.impl.java.clients.Clients;
import tukano.impl.java.servers.JavaShorts.Credentials;
import tukano.impl.rest.clients.RestShortsClient;
import tukano.kafka.lib.KafkaPublisher;
import tukano.kafka.lib.KafkaSubscriber;
import tukano.kafka.sync.SyncPoint;
import utils.DB;
import utils.Token;

public class JavaShortsRep implements ExtendedShortsRep, RecordProcessor {
	static final String KAFKA_BROKERS = "kafka:9092";

	static final String TOPIC = "X-SHORTS";

	final SyncPoint<Result<?>> sync;

	private long lastOffset = -1;

	static final String FROM_BEGINNING = "earliest";

	final KafkaPublisher sender;
	final KafkaSubscriber receiver;

	private final String COMMA = ",";

	private final JavaShorts jsResolver;

	private static Logger Log = Logger.getLogger(JavaShortsRep.class.getName());

	AtomicLong counter = new AtomicLong(totalShortsInDatabase());

	public JavaShortsRep() {
		this.sync = new SyncPoint<>();
		this.sender = KafkaPublisher.createPublisher(KAFKA_BROKERS);
		this.receiver = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(TOPIC), FROM_BEGINNING);

		this.jsResolver = new JavaShorts();
		this.receiver.start(false, this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<Short> createShort(String userId, String password) {
		Log.info(() -> format("createShort : userId = %s, pwd = %s\n", userId, password));

		Result<User> res = okUser(userId, password);

		if (!res.isOK()) {
			return Result.error(res.error());
		}

		var version = sender.publish(TOPIC, "create_short", userId + COMMA + System.currentTimeMillis());
		return (Result<Short>) sync.waitForResult(version);
	}

	public Result<Short> createShortAux(String userId, long time) {
		var shortId = format("%s-%d", userId, counter.incrementAndGet());
		var blobUrl = getLiveBlobsUrl(shortId);

		var shrt = new Short(shortId, userId, blobUrl, time, 0);

		return DB.insertOne(shrt);
	}

	protected Result<Short> shortXFromCache(String shortId) {
		try {
			var query = format("SELECT count(*) FROM Likes l WHERE l.shortId = '%s'", shortId);
			var likes = DB.sql(query, Long.class);

			return errorOrValue(getOne(shortId, Short.class), shrt -> shrt.copyWith(likes.get(0)));
		} catch (Exception e) {
			e.printStackTrace();
			return error(INTERNAL_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<Short> getShort(String shortId) {
		Log.info(() -> format("getShort : shortId = %s\n", shortId));

		if (shortId == null)
			return error(BAD_REQUEST);

		var version = sender.publish(TOPIC, "get_short", shortId + COMMA + System.currentTimeMillis());
		return (Result<Short>) sync.waitForResult(version);
	}

	protected Result<User> okUser(String userId, String pwd) {
		try {
			return jsResolver.usersCache.get(new Credentials(userId, pwd));
		} catch (Exception x) {
			x.printStackTrace();
			return Result.error(INTERNAL_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<Void> deleteShort(String shortId, String password) {
		Log.info(() -> format("deleteShort : userId = %s, pwd = %s\n", shortId, password));

		var version = sender.publish(TOPIC, "delete_short", shortId + COMMA + password);
		return (Result<Void>) sync.waitForResult(version);
	}

	@Override
	public Result<List<String>> getShorts(String userId) {
		return jsResolver.getShorts(userId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
		var VRS = sender.publish(TOPIC, "follow", userId1 + COMMA + userId2 + COMMA + isFollowing + COMMA + password);
		return (Result<Void>) sync.waitForResult(VRS);
	}

	@Override
	public Result<List<String>> followers(String userId, String password) {
		return jsResolver.followers(userId, password);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
		Log.info(() -> format("like : userId = %s, pwd = %s\n", shortId, password));

		var VRS = sender.publish(TOPIC, "like", shortId + COMMA + userId + COMMA + isLiked + COMMA + password);
		return (Result<Void>) sync.waitForResult(VRS);
	}

	@Override
	public Result<List<String>> likes(String shortId, String password) {
		return jsResolver.likes(shortId, password);
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		return jsResolver.getFeed(userId, password);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<Void> deleteAllShorts(String userId, String password, String token) {
		Log.info(() -> format("deleteAllShorts : userId = %s, pwd = %s\n", userId, password));

		return ((ExtendedShortsRep) Clients.ShortsClients.get()).notifyAllDeleteShorts(userId, password, token);
		
		
		//var VRS = sender.publish(TOPIC, "delete_all_shorts", userId + COMMA + password + COMMA + token);
		//return (Result<Void>) sync.waitForResult(VRS);
	}

	
	// Extended API
	private String getLiveBlobsUrl(String shortId) {
		URI[] knownUris = Discovery.getInstance().knownUrisOf(Blobs.NAME, 0);

		return Arrays.stream(knownUris).map(uri -> String.format("%s/%s/%s", uri.toString(), Blobs.NAME, shortId))
				.collect(Collectors.joining("|", "", ""));

	}

	private long totalShortsInDatabase() {
		var hits = DB.sql("SELECT count('*') FROM Short", Long.class);
		return 1L + (hits.isEmpty() ? 0L : hits.get(0));
	}

	@Override
	public void onReceive(ConsumerRecord<String, String> record) {
		Log.info(() -> format("onReceive : key = %s, value = %s\n", record.key(), record.value()));
		var version = record.offset();

		if (version <= lastOffset) {
			return;
		}

		var parts = record.value().split(COMMA);
		switch (record.key()) {
		case "create_short":
			var userId = parts[0];
			var time = Long.parseLong(parts[1]);
			sync.setResult(version, createShortAux(userId, time));
			break;
		case "get_short":
			var shortId = parts[0];
			sync.setResult(version, jsResolver.getShort(shortId));
			break;
		case "delete_short":
			shortId = parts[0];
			var password = parts[1];
			sync.setResult(version, jsResolver.deleteShort(shortId, password));
			break;
		case "follow":
			var userId1 = parts[0];
			var userId2 = parts[1];
			var isFollowing = Boolean.parseBoolean(parts[2]);
			password = parts[3];
			sync.setResult(version, jsResolver.follow(userId1, userId2, isFollowing, password));
			break;
		case "like":
			shortId = parts[0];
			userId1 = parts[1];
			var isLiked = Boolean.parseBoolean(parts[2]);
			password = parts[3];
			sync.setResult(version, jsResolver.like(shortId, userId1, isLiked, password));
			break;
		case "delete_all_Shorts":
			userId1 = parts[0];
			password = parts[1];
			var token = parts[2];
			sync.setResult(version, jsResolver.deleteAllShorts(userId1, password, token));
			break;
		}

		lastOffset = version;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result<Void> notifyAllDeleteShorts(String userId, String password, String token) {
		// TODO Auto-generated method stub
		var VRS = sender.publish(TOPIC, "delete_all_shorts", userId + COMMA + password + COMMA + token);
		return (Result<Void>) sync.waitForResult(VRS);
	}

}