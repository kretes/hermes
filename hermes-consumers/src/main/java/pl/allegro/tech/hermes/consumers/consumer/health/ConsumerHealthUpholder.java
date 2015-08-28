package pl.allegro.tech.hermes.consumers.consumer.health;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.allegro.tech.hermes.api.SubscriptionName;
import pl.allegro.tech.hermes.common.time.Clock;
import pl.allegro.tech.hermes.infrastructure.zookeeper.ZookeeperPaths;

import java.io.IOException;

public class ConsumerHealthUpholder {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerHealthUpholder.class);

    private final PersistentEphemeralNode ephemeralNode;
    private final SubscriptionName subscriptionName;

    public ConsumerHealthUpholder(CuratorFramework curatorFramework, ZookeeperPaths zookeeperPaths, SubscriptionName subscriptionName,
                                  Clock clock, String hostname) throws Exception {
        this.subscriptionName = subscriptionName;

        ephemeralNode = new PersistentEphemeralNode(
            curatorFramework,
            PersistentEphemeralNode.Mode.EPHEMERAL,
            zookeeperPaths.subscriptionHealthPath(subscriptionName) + "/" + consumerNodeName(hostname, clock.getTime()),
            "alive".getBytes()
        );
    }

    public void start() {
        ephemeralNode.start();
    }

    public void stop() {
        try {
            ephemeralNode.close();
        } catch (IOException e) {
            logger.error("Error while closing health upholder for consumer {}.", subscriptionName.getName(), e);
        }
    }

    private String consumerNodeName(String hostname, long time) {
        return String.format("consumer-%s-%s", hostname, time);
    }

}
