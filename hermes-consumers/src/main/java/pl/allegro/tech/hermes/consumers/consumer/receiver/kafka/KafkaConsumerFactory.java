package pl.allegro.tech.hermes.consumers.consumer.receiver.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

public class KafkaConsumerFactory {

    Consumer<byte[], byte[]> create(Properties properties) {
        return new KafkaConsumer<>(properties);
    }
}
