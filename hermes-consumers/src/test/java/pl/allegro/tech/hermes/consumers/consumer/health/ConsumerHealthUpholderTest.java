package pl.allegro.tech.hermes.consumers.consumer.health;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pl.allegro.tech.hermes.api.SubscriptionName;
import pl.allegro.tech.hermes.api.TopicName;
import pl.allegro.tech.hermes.common.time.Clock;
import pl.allegro.tech.hermes.common.util.HostnameResolver;
import pl.allegro.tech.hermes.infrastructure.zookeeper.ZookeeperPaths;
import pl.allegro.tech.hermes.test.helper.zookeeper.ZookeeperBaseTest;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerHealthUpholderTest extends ZookeeperBaseTest {

    @Mock
    private Clock clock;

    @Mock
    private HostnameResolver hostnameResolver;

    private ZookeeperPaths zookeeperPaths = new ZookeeperPaths("/hermes");

    private SubscriptionName subscriptionName = new SubscriptionName("sub", TopicName.fromQualifiedName("group.topic"));

    @Before
    public void setUp() throws Exception {
        deleteData(zookeeperPaths.subscriptionHealthPath(subscriptionName));
        createPath(zookeeperPaths.subscriptionPath(subscriptionName.getTopicName(), subscriptionName.getName()));
    }

    @Test
    public void shouldUpholdConsumerHealth() throws Exception {
        // given
        when(hostnameResolver.resolve()).thenReturn("hostname");
        when(clock.getTime()).thenReturn(5L);
        ConsumerHealthUpholder consumerHealthUpholder = new ConsumerHealthUpholder(
            zookeeperClient, zookeeperPaths, subscriptionName, hostnameResolver, clock
        );

        // when
        consumerHealthUpholder.start();

        // then
        wait.untilZookeeperPathIsCreated(zookeeperPaths.subscriptionHealthPath(subscriptionName) + "/consumer-hostname-5");

        // after
        consumerHealthUpholder.stop();
    }

    @Test
    public void shouldRemoveNodeAfterStoppingHealthUpholder() throws Exception {
        // given
        ConsumerHealthUpholder consumerHealthUpholder = new ConsumerHealthUpholder(
            zookeeperClient, zookeeperPaths, subscriptionName, hostnameResolver, clock);
        consumerHealthUpholder.start();
        wait.untilZookeeperPathIsNotEmpty(zookeeperPaths.subscriptionHealthPath(subscriptionName));

        // when
        consumerHealthUpholder.stop();

        // then
        wait.untilZookeeperPathIsEmpty(zookeeperPaths.subscriptionHealthPath(subscriptionName));
    }

}