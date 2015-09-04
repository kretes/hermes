package pl.allegro.tech.hermes.consumers.consumer.receiver.kafka;

import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.api.Topic;
import pl.allegro.tech.hermes.common.config.ConfigFactory;
import pl.allegro.tech.hermes.common.config.Configs;
import pl.allegro.tech.hermes.common.kafka.ConsumerGroupId;
import pl.allegro.tech.hermes.common.kafka.KafkaNamesMapper;
import pl.allegro.tech.hermes.common.message.wrapper.MessageContentWrapper;
import pl.allegro.tech.hermes.common.metric.HermesMetrics;
import pl.allegro.tech.hermes.common.metric.Timers;
import pl.allegro.tech.hermes.common.time.Clock;
import pl.allegro.tech.hermes.consumers.consumer.receiver.MessageReceiver;
import pl.allegro.tech.hermes.consumers.consumer.receiver.ReceiverFactory;

import javax.inject.Inject;
import java.util.Properties;

public class KafkaMessageReceiverFactory implements ReceiverFactory {

    private final ConfigFactory configFactory;
    private final MessageContentWrapper messageContentWrapper;
    private final HermesMetrics hermesMetrics;
    private final Clock clock;
    private final KafkaNamesMapper kafkaNamesMapper;
    private final KafkaConsumerFactory kafkaConsumerFactory;

    @Inject
    public KafkaMessageReceiverFactory(ConfigFactory configFactory, MessageContentWrapper messageContentWrapper,
                                       HermesMetrics hermesMetrics, Clock clock, KafkaNamesMapper kafkaNamesMapper, KafkaConsumerFactory kafkaConsumerFactory) {
        this.configFactory = configFactory;
        this.messageContentWrapper = messageContentWrapper;
        this.hermesMetrics = hermesMetrics;
        this.clock = clock;
        this.kafkaNamesMapper = kafkaNamesMapper;
        this.kafkaConsumerFactory = kafkaConsumerFactory;
    }

    @Override
    public MessageReceiver createMessageReceiver(Topic receivingTopic, Subscription subscription) {
        return createKafkaConsumerMessageReceiver(receivingTopic, subscription);
    }

    MessageReceiver createKafkaConsumerMessageReceiver(Topic receivingTopic, Subscription subscription) {
        Properties consumerConfig = createConsumerProperties(kafkaNamesMapper.toConsumerGroupId(subscription));
        return new KafkaConsumerMessageReceiver(
                kafkaConsumerFactory.create(consumerConfig),
                receivingTopic,
                messageContentWrapper,
                hermesMetrics.timer(Timers.CONSUMER_READ_LATENCY),
                clock);
    }

    private Properties createConsumerProperties(ConsumerGroupId groupId) {
        Properties props = new Properties();

        props.put("group.id", groupId.asString());
        props.put("enable.auto.commit", false);
        props.put("session.timeout.ms", configFactory.getIntProperty(Configs.KAFKA_CONSUMER_TIMEOUT_MS));
        props.put("fetch.min.bytes", configFactory.getIntProperty(Configs.KAFKA_CONSUMER_FETCH_MIN_BYTES));
        props.put("fetch.max.wait.ms", configFactory.getIntProperty(Configs.KAFKA_CONSUMER_FETCH_MAX_WAIT_MS));
        props.put("max.partition.fetch.bytes", configFactory.getIntProperty(Configs.KAFKA_CONSUMER_MAX_PARTITION_FETCH_BYTES));
        props.put("auto.offset.reset", "latest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("bootstrap.servers", configFactory.getStringProperty(Configs.KAFKA_BROKER_LIST));

        return props;
    }

}
