package pl.allegro.tech.hermes.common.kafka

import pl.allegro.tech.hermes.api.Topic
import spock.lang.Specification
import spock.lang.Unroll

import static pl.allegro.tech.hermes.api.Topic.Builder.topic

class KafkaNamesMapperTest extends Specification {

    @Unroll
    def "should map topic '#topicName' to kafka topic '#kafkaTopicName' for namespace '#namespace'"() {
        given:
        def mapper = new KafkaNamesMapper(namespace)

        expect:
        mapper.toKafkaTopics(topic().applyDefaults().withName(topicName).build()).primary.name() == KafkaTopicName.valueOf(kafkaTopicName)

        where:
        namespace | topicName     | kafkaTopicName
        "ns"      | "group.topic" | "ns_group.topic"
        ""        | "group.topic" | "group.topic"
    }

    @Unroll
    def "should map subscription id '#subscriptionId' to consumer group '#consumerGroupId' for namespace '#namespace'"() {
        given:
        def mapper = new KafkaNamesMapper(namespace)

        expect:
        mapper.toConsumerGroupId(subscriptionId) == ConsumerGroupId.valueOf(consumerGroupId)

        where:
        namespace | subscriptionId     | consumerGroupId
        "ns"      | "subscription_id" | "ns_subscription_id"
        ""        | "subscription_id" | "subscription_id"
    }

    def "should append '_avro' suffix for topics of type AVRO"() {
        given:
        def mapper = new KafkaNamesMapper("")
        def avroTopic = topic().withName("group", "topic").withContentType(Topic.ContentType.AVRO).build()

        expect:
        mapper.toKafkaTopics(avroTopic).primary.name() == KafkaTopicName.valueOf("group.topic_avro")
    }

    def "should map to topics with secondary json topic for topics migrated from json to avro"() {
        given:
        def mapper = new KafkaNamesMapper("")
        def migratedTopic = topic().withName("group", "topic").withContentType(Topic.ContentType.AVRO).migratedFromJsonType().build()

        when:
        def topics = mapper.toKafkaTopics(migratedTopic)

        then:
        topics.primary.contentType() == Topic.ContentType.AVRO
        topics.secondary.present
        topics.secondary.get().contentType() == Topic.ContentType.JSON
    }

}
