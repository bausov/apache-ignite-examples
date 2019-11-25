package eu.bausov.gridgainbinaryobjects;

import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.concurrent.TimeUnit;

/**
 * Created by Stanislav Bausov on 01.11.2019.
 */
@Slf4j
@SpringBootTest
class GridGainContainersTest2 {
    @ClassRule
    public static Network network = Network
            .newNetwork();
    @ClassRule
    public static GenericContainer zookeeper = new GenericContainer("confluentinc/cp-zookeeper:5.3.1")
            .withNetwork(network)
            .withNetworkAliases("zookeeper")
            .withEnv("ZOOKEEPER_CLIENT_PORT", "2181");
    @ClassRule
    public static GenericContainer gridGain = new GenericContainer("gridgain/community:8.7.7")
            .withNetwork(network)
            .withNetworkAliases("zookeeper")
            .withEnv("ZOOKEEPER_CONNECT", "zookeeper:2181")
            .withEnv("CONFIG_URI", "/config-file.xml")
            .withEnv("OPTION_LIBS", "ignite-zookeeper")
            .withClasspathResourceMapping("ignite-config.xml", "/config-file.xml", BindMode.READ_ONLY);

    @BeforeEach
    void setUp() {
        zookeeper.start();
        gridGain.dependsOn(zookeeper);
        gridGain.start();

        System.out.println(network.getId());
    }

    @Test
    void testSimplePutAndGet() throws InterruptedException {
        while (true) {
            TimeUnit.SECONDS.sleep(1000);
        }
    }
}