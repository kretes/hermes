package pl.allegro.tech.hermes.api;

public class SubscriptionName {

    private String name;
    private TopicName topicName;

    private SubscriptionName() {
    }

    public SubscriptionName(String name, TopicName topicName) {
        this.name = name;
        this.topicName = topicName;
    }

    public String getName() {
        return name;
    }

    public TopicName getTopicName() {
        return topicName;
    }

    public static SubscriptionName from(Subscription subscription) {
        return new SubscriptionName(subscription.getName(), subscription.getTopicName());
    }
}
