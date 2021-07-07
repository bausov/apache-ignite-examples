package eu.bausov.gridgainbinaryobjects;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.zk.ZookeeperDiscoverySpi;
import org.jetbrains.annotations.NotNull;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.UUID;

import static org.apache.ignite.cluster.ClusterState.ACTIVE;
import static org.testcontainers.utility.DockerImageName.parse;

/**
 * Created by Stanislav Bausov on 01.11.2019.
 */
@Slf4j
@SpringBootTest
class GridGainContainersTest6 {
    public static final Network NETWORK = Network.newNetwork();
    public static final GenericContainer<?> ZOOKEEPER = new GenericContainer<>(parse("confluentinc/cp-zookeeper:6.2.0"))
            .withNetwork(NETWORK)
            .withNetworkAliases("zookeeper")
            .withExposedPorts(2181)
            .withEnv("ZOOKEEPER_CLIENT_PORT", "2181")
            .withEnv("ZOOKEEPER_TICK_TIME", "2000");
    @ClassRule
    public static GenericContainer<?> IGNITE = new FixedHostPortGenericContainer(parse("apacheignite/ignite:2.10.0").asCanonicalNameString())
            .withFixedExposedPort(11211, 11211)
            .withFixedExposedPort(47100, 47100)
            .withFixedExposedPort(47500, 47500)
            .withFixedExposedPort(49112, 49112)
            .withFixedExposedPort(10800, 49112)
            .withNetwork(NETWORK)
            .withNetworkAliases("node")
//            .withNetworkMode("host")
//            .withExtraHost("node", "127.0.0.9")
            .withEnv("CONFIG_URI", "/config-file.xml")
            .withEnv("OPTION_LIBS", "ignite-zookeeper")
            .withClasspathResourceMapping("ignite-config.xml", "/config-file.xml", BindMode.READ_ONLY);

    @BeforeEach
    void setUp() {
        ZOOKEEPER.start();
        IGNITE
                .dependsOn(ZOOKEEPER)
                .withEnv("ZOOKEEPER_CONNECT", "zookeeper:2181")
                .start();
    }

    @NotNull
    private String zkConnect() {
        final String address = ZOOKEEPER.getContainerIpAddress();
        final Integer port = ZOOKEEPER.getFirstMappedPort();

        return String.format("%s:%s", address, port);
    }

    @Test
    void testSimplePutAndGet() {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
//        cfg.setPeerClassLoadingEnabled(false); // todo as remote
        cfg.setPeerClassLoadingEnabled(true);

        final ZookeeperDiscoverySpi zkDiscoSpi = new ZookeeperDiscoverySpi();
        zkDiscoSpi.setZkConnectionString(zkConnect());
        zkDiscoSpi.setZkRootPath("/apacheIgnite");
        zkDiscoSpi.setSessionTimeout(30_000);
        zkDiscoSpi.setJoinTimeout(10_000);

        cfg.setDiscoverySpi(zkDiscoSpi);

        // Starting a thick Java client that will connect to the cluster.
        Ignite ignite = Ignition.start(cfg);
        ignite.cluster().state(ACTIVE);
        IgniteCache<Integer, BinaryObject> binaryCache = null;

        // Create a regular Person object and put it into the cache.
        Person person = new Person(1, "FirstPerson");
        ignite.getOrCreateCache("personCache").put(1, person);

        // Get an instance of binary-enabled cache.
        binaryCache = ignite.cache("personCache").withKeepBinary();
        BinaryObject binaryPerson = binaryCache.get(1);

        log.info("\n\n\n>>>> " + binaryPerson.<Person>deserialize().toString());

        // The EntryProcessor is to be executed for this key.
        final PersonProcessor entryProcessor = new PersonProcessor();
        runProcessor(ignite, binaryCache, entryProcessor);
        runProcessor(ignite, binaryCache, entryProcessor);
        runProcessor(ignite, binaryCache, entryProcessor);

        ignite.close();
    }

    private void runProcessor(Ignite ignite, IgniteCache<Integer, BinaryObject> binaryCache, PersonProcessor entryProcessor) {
        ignite.cache("personCache")
                .<Integer, BinaryObject>withKeepBinary()
                .invoke(1, entryProcessor, UUID.randomUUID().toString());

        log.info("\n\n\n>>>> " + binaryCache.get(1).<Person>deserialize().toString() + "\n\n\n");
    }
}