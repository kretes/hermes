package pl.allegro.tech.hermes.consumers.consumer.health;

import org.apache.curator.framework.CuratorFramework;
import pl.allegro.tech.hermes.api.SubscriptionName;
import pl.allegro.tech.hermes.common.di.CuratorType;
import pl.allegro.tech.hermes.common.time.Clock;
import pl.allegro.tech.hermes.infrastructure.zookeeper.ZookeeperPaths;

import javax.inject.Inject;
import javax.inject.Named;

public class ConsumerHealthUpholderFactory {

    private final CuratorFramework curatorFramework;
    private final ZookeeperPaths zookeeperPaths;
    private final Clock clock;
    private final String hostname;

    @Inject
    public ConsumerHealthUpholderFactory(@Named(CuratorType.HERMES) CuratorFramework curatorFramework,
                                         ZookeeperPaths zookeeperPaths,
                                         @Named("hostname") String hostname,
                                         Clock clock) {

        this.curatorFramework = curatorFramework;
        this.zookeeperPaths = zookeeperPaths;
        this.hostname = hostname;
        this.clock = clock;
    }

    public ConsumerHealthUpholder create(SubscriptionName subscriptionName) throws Exception {
        return new ConsumerHealthUpholder(curatorFramework, zookeeperPaths, subscriptionName, clock, hostname);
    }

}
