package tukano.kafka.examples.ex1;

import java.util.List;

import tukano.kafka.lib.KafkaSubscriber;
import tukano.kafka.lib.KafkaUtils;

public class KafkaReceiver {
	private static final String FROM_BEGINNING = "earliest";

	public static void main(String[] args) {
		KafkaUtils.createTopic(KafkaSender.TOPIC, 1, 1);
		
		var subscriber = KafkaSubscriber.createSubscriber(KafkaSender.KAFKA_BROKERS, List.of(KafkaSender.TOPIC), FROM_BEGINNING);

		subscriber.start(true, (r) -> {
			System.out.printf("Topic: %s, offset: %d, value: %s\n", r.topic(), r.offset(), r.value());
		});
	}
}
