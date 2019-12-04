package eu.bausov.gridgainbinaryobjects;

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
import org.testcontainers.containers.GenericContainer;

/**
 * Created by Stanislav Bausov on 01.11.2019.
 */
@Slf4j
@SpringBootTest
class GridGainContainersTest3 {
    //    @ClassRule
//    public static GenericContainer zookeeper = new GenericContainer("confluentinc/cp-zookeeper:5.3.1")
//    public static GenericContainer zookeeper = new GenericContainer("zookeeper:3.5.6")
//            .withNetworkMode("host")
//            .withEnv("ZOOKEEPER_CLIENT_PORT", "2181");
    @ClassRule
    public static GenericContainer gridGain = new GenericContainer("gridgain/community:8.7.7")
            .withNetworkMode("host")
            .withEnv("ZOOKEEPER_CONNECT", "localhost:2181")
            .withEnv("CONFIG_URI", "/config-file.xml")
            .withEnv("OPTION_LIBS", "ignite-zookeeper")
            .withClasspathResourceMapping("ignite-config.xml", "/config-file.xml", BindMode.READ_ONLY);

    @BeforeEach
    void setUp() {
//        zookeeper.start();
//        gridGain.dependsOn(zookeeper);
        gridGain.start();
    }

    @Test
    void testSimplePutAndGet() throws InterruptedException {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
//        cfg.setPeerClassLoadingEnabled(false); // todo as remote
        cfg.setPeerClassLoadingEnabled(true);

        final ZookeeperDiscoverySpi zkDiscoSpi = new ZookeeperDiscoverySpi();
        zkDiscoSpi.setZkConnectionString("localhost:2181");
        zkDiscoSpi.setZkRootPath("/apacheIgnite");
        zkDiscoSpi.setSessionTimeout(30_000);
        zkDiscoSpi.setJoinTimeout(10_000);

        cfg.setDiscoverySpi(zkDiscoSpi);

        // Starting a thick Java client that will connect to the cluster.
        Ignite ignite = Ignition.start(cfg);
        IgniteCache<Integer, BinaryObject> binaryCache = null;

        // Create a regular Person object and put it into the cache.
        Person person = new Person(1, "FirstPerson");
        ignite.getOrCreateCache("personCache").put(1, person);

        // Get an instance of binary-enabled cache.
        binaryCache = ignite.cache("personCache").withKeepBinary();
        BinaryObject binaryPerson = binaryCache.get(1);

        log.info("\n\n\n>>>> " + binaryPerson.<Person>deserialize().toString());

        // The EntryProcessor is to be executed for this key.
        int key = 1;
        ignite.cache("personCache")
                .<Integer, BinaryObject>withKeepBinary()
                .invoke(key, (entry, arguments) -> {
                    // Create a builder from the old value.
                    BinaryObjectBuilder bldr = entry.getValue().toBuilder();

                    //Update the field in the builder.
                    bldr.setField("name", "GridGain");

                    // Set new value to the entry.
                    entry.setValue(bldr.build());

                    return null;
                });

        log.info("\n\n\n>>>> " + binaryCache.get(1).<Person>deserialize().toString());

        ignite.close();
    }
}