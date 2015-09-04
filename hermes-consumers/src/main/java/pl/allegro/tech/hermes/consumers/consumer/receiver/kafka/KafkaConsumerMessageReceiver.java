package pl.allegro.tech.hermes.consumers.consumer.receiver.kafka;

import com.codahale.metrics.Timer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import pl.allegro.tech.hermes.api.Topic;
import pl.allegro.tech.hermes.common.exception.InternalProcessingException;
import pl.allegro.tech.hermes.common.message.wrapper.MessageContentWrapper;
import pl.allegro.tech.hermes.common.message.wrapper.UnwrappedMessageContent;
import pl.allegro.tech.hermes.common.time.Clock;
import pl.allegro.tech.hermes.consumers.consumer.Message;
import pl.allegro.tech.hermes.consumers.consumer.receiver.MessageReceiver;
import pl.allegro.tech.hermes.consumers.consumer.receiver.MessageReceivingTimeoutException;

import java.util.Arrays;
import java.util.Iterator;

public class KafkaConsumerMessageReceiver implements MessageReceiver {

    private final MessageReceivingTimeoutException pollTimeoutException;
    private final Topic topic;
    private final MessageContentWrapper contentWrapper;
    private final Timer readingTimer;
    private final Clock clock;

    private final Consumer<byte[], byte[]> consumer;
    private ConsumerRecords<byte[], byte[]> records;
    private Iterator<ConsumerRecord<byte[], byte[]>> iterator;


    public KafkaConsumerMessageReceiver(Consumer<byte[], byte[]> consumer, Topic topic, MessageContentWrapper contentWrapper,
                                        Timer readingTimer, Clock clock) {
        this.topic = topic;
        this.contentWrapper = contentWrapper;
        this.readingTimer = readingTimer;
        this.clock = clock;
        this.consumer = consumer;
        this.pollTimeoutException = new MessageReceivingTimeoutException("No messages received for topic " + topic.getQualifiedName());

        this.consumer.subscribe(Arrays.asList(topic.getQualifiedName()));
    }

    @Override
    public Message next() {

        try (Timer.Context readingTimerContext = readingTimer.time()) {

            ConsumerRecord<byte[], byte[]> message = pollNext();
            UnwrappedMessageContent unwrappedContent;
            try {
                unwrappedContent = contentWrapper.unwrap(message.value(), topic);
            } catch (Exception e) {
                throw new InternalProcessingException("Message receiving failed", e);
            }
            return new Message(
                    unwrappedContent.getMessageMetadata().getId(),
                    message.offset(),
                    message.partition(),
                    topic.getQualifiedName(),
                    unwrappedContent.getContent(),
                    unwrappedContent.getMessageMetadata().getTimestamp(),
                    clock.getTime());

        }
    }

    private ConsumerRecord<byte[], byte[]> pollNext() {
        if (records == null || records.isEmpty() || !iterator.hasNext()) {
            records = consumer.poll(10_000);
            iterator = records.iterator();

            if (records.isEmpty()) {
                throw pollTimeoutException;
            }
        }
        return iterator.next();
    }

    @Override
    public void stop() {
        consumer.close();
    }
}
