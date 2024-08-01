package tukano.impl.java.servers;

import static java.lang.String.format;
import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;
import static tukano.api.java.Result.ErrorCode.BAD_REQUEST;
import static tukano.api.java.Result.ErrorCode.CONFLICT;
import static tukano.api.java.Result.ErrorCode.FORBIDDEN;
import static tukano.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static tukano.api.java.Result.ErrorCode.NOT_FOUND;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Logger;

import tukano.api.java.Result;
import tukano.impl.api.java.ExtendedBlobs;
import tukano.impl.java.clients.Clients;
import utils.Hash;
import utils.Hex;
import utils.IOAuth;
import utils.ProxyState;
import utils.Token;

public class JavaBlobsAuth implements ExtendedBlobs {

	private static final String BLOBS_ROOT_DIR = "/tmp/blobs/";

	private static Logger Log = Logger.getLogger(JavaBlobsAuth.class.getName());

	private static final int CHUNK_SIZE = 4096;

	@Override
	public Result<Void> upload(String blobId, byte[] bytes) {
		Log.info(() -> format("upload : blobId = %s, sha256 = %s\n", blobId, Hex.of(Hash.sha256(bytes))));

		if (!validBlobId(blobId))
			return error(FORBIDDEN);

		var dir = toDirPath(blobId);
		if (dir == null)
			return error(BAD_REQUEST);

		var parts = blobId.split("-");
		var fullPath = dir + "/" + parts[1];

		if (IOAuth.fileExists(dir, parts[1])) {
			if (Arrays.equals(Hash.sha256(bytes), Hash.sha256(IOAuth.readBytes(fullPath))))
				return ok();
			else {
				if (ProxyState.isStateless())
					delete(blobId, Token.get());
				else 
					return error(CONFLICT);
			}		
		}

		IOAuth.write(fullPath, bytes);
		return ok();
	}

	@Override
	public Result<byte[]> download(String blobId) {
		Log.info(() -> format("download : blobId = %s\n", blobId));

		var dir = toDirPath(blobId);
		if (dir == null)
			return error(BAD_REQUEST);

		var parts = blobId.split("-");
		if (IOAuth.fileExists(dir, parts[1]))
			return ok(IOAuth.readBytes(dir + "/" + parts[1]));
		else
			return error(NOT_FOUND);

	}

	@Override
	public Result<Void> downloadToSink(String blobId, Consumer<byte[]> sink) {
		Log.info(() -> format("downloadToSink : blobId = %s\n", blobId));

		var dir = toDirPath(blobId);
		if (dir == null)
			return error(BAD_REQUEST);

		var parts = blobId.split("-");
		try (var fis = IOAuth.readStream(dir + "/" + parts[1])) {
			int n;
			var chunk = new byte[CHUNK_SIZE];
			while ((n = fis.read(chunk)) > 0)
				sink.accept(Arrays.copyOf(chunk, n));

			return ok();
		} catch (IOException x) {
			return error(INTERNAL_ERROR);
		}
	}

	@Override
	public Result<Void> delete(String blobId, String token) {
		Log.info(() -> format("delete : blobId = %s, token=%s\n", blobId, token));

		if (!Token.matches(token))
			return error(FORBIDDEN);

		var dir = toDirPath(blobId);
		if (dir == null)
			return error(BAD_REQUEST);

		var parts = blobId.split("-");
		if (!IOAuth.fileExists(dir, parts[1]))
			return error(NOT_FOUND);

		IOAuth.delete(dir + "/" + parts[1]);

		return ok();
	}

	@Override
	public Result<Void> deleteAllBlobs(String userId, String token) {
		Log.info(() -> format("deleteAllBlobs : userId = %s, token=%s\n", userId, token));

		if (!Token.matches(token))
			return error(FORBIDDEN);

		try {
			var path = BLOBS_ROOT_DIR + userId;
			for (String fileName : IOAuth.listDirFiles(path)) {
				IOAuth.delete(path + "/" + fileName);
			}

			IOAuth.delete(path);
			return ok();
		} catch (Exception e) {
			e.printStackTrace();
			return error(INTERNAL_ERROR);
		}
	}

	private boolean validBlobId(String blobId) {
		return Clients.ShortsClients.get().getShort(blobId).isOK();
	}

	private String toDirPath(String blobId) {
		var parts = blobId.split("-");
		if (parts.length != 2)
			return null;

		var res = BLOBS_ROOT_DIR + parts[0];
		return res;
	}
}
