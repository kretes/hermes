package pl.allegro.tech.hermes.consumers.consumer.offset;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.common.config.ConfigFactory;
import pl.allegro.tech.hermes.common.config.Configs;
import pl.allegro.tech.hermes.common.kafka.offset.PartitionOffset;
import pl.allegro.tech.hermes.common.metric.HermesMetrics;
import pl.allegro.tech.hermes.common.time.Clock;
import pl.allegro.tech.hermes.consumers.consumer.Message;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionOffsetCommitQueues {

    private final LoadingCache<TopicPartition, OffsetCommitQueue> queues;

    public SubscriptionOffsetCommitQueues(Subscription subscription, HermesMetrics hermesMetrics, Clock clock, ConfigFactory configFactory) {
        this.queues = CacheBuilder.newBuilder()
                .build(new OffsetCommitQueueLoader(subscription, hermesMetrics, clock, configFactory));
    }

    public void put(Message message) {
        OffsetCommitQueue helper = queues.getUnchecked(new TopicPartition(message.getKafkaTopic(), message.getPartition()));
        helper.put(message.getOffset());
    }

    public void remove(Message message) {
        OffsetCommitQueue offsetCommitQueue = queues.getUnchecked(new TopicPartition(message.getKafkaTopic(), message.getPartition()));
        offsetCommitQueue.markDelivered(message.getOffset());
    }

    public List<PartitionOffset> getOffsetsToCommit() {
        List<PartitionOffset> offsets = new ArrayList<>();
        queues.asMap().forEach((topicAndPartition, queue) -> queue.poll().ifPresent(offset ->
                offsets.add(new PartitionOffset(topicAndPartition.getTopic(), offset, topicAndPartition.getPartition()))));
        return offsets;
    }

    private static final class OffsetCommitQueueLoader extends CacheLoader<TopicPartition, OffsetCommitQueue> {

        private final Subscription subscription;
        private final HermesMetrics hermesMetrics;
        private final Clock clock;
        private final ConfigFactory configFactory;

        public OffsetCommitQueueLoader(Subscription subscription, HermesMetrics hermesMetrics, Clock clock, ConfigFactory configFactory) {
            this.subscription = subscription;
            this.hermesMetrics = hermesMetrics;
            this.clock = clock;
            this.configFactory = configFactory;
        }

        @Override
        public OffsetCommitQueue load(TopicPartition topicPartition) throws Exception {
            return new OffsetCommitQueue(new OffsetCommitQueueMonitor(subscription, topicPartition, hermesMetrics, clock,
                    configFactory.getIntProperty(Configs.CONSUMER_OFFSET_COMMIT_QUEUE_ALERT_MINIMAL_IDLE_PERIOD),
                    configFactory.getIntProperty(Configs.CONSUMER_OFFSET_COMMIT_QUEUE_ALERT_SIZE)));
        }
    }

}
