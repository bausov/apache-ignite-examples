package eu.bausov.gridgainbinaryobjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.zk.ZookeeperDiscoverySpi;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.TimeUnit;

/**
 * Created by Stanislav Bausov on 01.11.2019.
 */
@Slf4j
@SpringBootTest
class GridGainContainersTest {
    @ClassRule
    public static GridGainContainer igniteContainer = new GridGainContainer()
//            .withExposedPorts(
//                    11211, 47100, 47500, 49112
//            )
            .withFixedExposedPort(11211, 11211)
            .withFixedExposedPort(47100, 47100)
            .withFixedExposedPort(47500, 47500)
            .withFixedExposedPort(49112, 49112)
            .withClasspathResourceMapping("ignite-config.xml", "/config-file.xml", BindMode.READ_ONLY);

    @ClassRule
    public static ZookeeperContainer zookeeperContainer = new ZookeeperContainer()
            .withExposedPorts(2181);

    @BeforeEach
    void setUp() {
        zookeeperContainer.start();

        String address = zookeeperContainer.getContainerIpAddress();
        Integer port = zookeeperContainer.getFirstMappedPort();
        final var zookeeperConnect = address + ":" + port;

        igniteContainer.addEnv("ZOOKEEPER_CONNECT", zookeeperConnect);
        igniteContainer.addEnv("CONFIG_URI", "/config-file.xml");
        igniteContainer.addEnv("OPTION_LIBS", "ignite-zookeeper");
//        igniteContainer.addEnv("OPTION_LIBS", "ignite-log4j, ignite-spring, ignite-indexing, ignite-zookeeper");
        igniteContainer.dependsOn(zookeeperContainer);
//        igniteContainer.setCommand("-e CONFIG_URI=/config-file.xml");
        igniteContainer.start();
    }

    @Test
    void testSimplePutAndGet() {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
        cfg.setPeerClassLoadingEnabled(false); // todo as remote

        String address = zookeeperContainer.getContainerIpAddress();
        Integer port = zookeeperContainer.getFirstMappedPort();
        final var zookeeperConnect = address + ":" + port;

        final ZookeeperDiscoverySpi zkDiscoSpi = new ZookeeperDiscoverySpi();
        zkDiscoSpi.setZkConnectionString(zookeeperConnect);
//        zkDiscoSpi.setZkConnectionString("localhost:2181");
//        zkDiscoSpi.setZkRootPath("/ignite/dev");
//        zkDiscoSpi.setZkRootPath("/apacheIgnite");
        zkDiscoSpi.setSessionTimeout(30_000);
        zkDiscoSpi.setJoinTimeout(10_000);

        cfg.setDiscoverySpi(zkDiscoSpi);

        try {
            TimeUnit.SECONDS.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Starting a thick Java client that will connect to the cluster.
        Ignite ignite = Ignition.start(cfg);

        // Create a regular Person object and put it into the cache.
        Person person = new Person(1, "FirstPerson");
        ignite.getOrCreateCache("personCache").put(1, person);

        // Get an instance of binary-enabled cache.
        IgniteCache<Integer, BinaryObject> binaryCache = ignite.cache("personCache").withKeepBinary();
        BinaryObject binaryPerson = binaryCache.get(1);

        log.info(">>>> " + binaryPerson.<Person>deserialize().toString());

        // The EntryProcessor is to be executed for this key.
        int key = 1;
        ignite.cache("personCache").<Integer, BinaryObject>withKeepBinary().invoke(key,
                (entry, arguments) -> {
                    // Create a builder from the old value.
                    BinaryObjectBuilder bldr = entry.getValue().toBuilder();

                    //Update the field in the builder.
                    bldr.setField("name", "GridGain");

                    // Set new value to the entry.
                    entry.setValue(bldr.build());

                    return null;
                });

        log.info(">>>> " + binaryCache.get(1).<Person>deserialize().toString());

        ignite.close();
    }

    //    static class GridGainContainer extends GenericContainer<GridGainContainer> {
    static class GridGainContainer extends FixedHostPortGenericContainer<GridGainContainer> {

        GridGainContainer() {
            super("gridgain/community:8.7.7");
        }
    }

    static class ZookeeperContainer extends GenericContainer<ZookeeperContainer> {

        ZookeeperContainer() {
            super("zookeeper:3.5.6");
        }
    }

    @Data
    @AllArgsConstructor
    private static class Person {
        private int age;
        private String name;
    }
}