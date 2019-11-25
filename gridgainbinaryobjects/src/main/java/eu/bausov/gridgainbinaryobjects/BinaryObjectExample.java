package eu.bausov.gridgainbinaryobjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.binary.BinaryObjectBuilder;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Created by Stanislav Bausov on 01.11.2019.
 */
public class BinaryObjectExample {
    private static final Logger log = LoggerFactory.getLogger(BinaryObjectExample.class);

    public static void main(String[] args) throws IgniteException {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
        cfg.setPeerClassLoadingEnabled(false);

        // Setting up an IP Finder to ensure the client can locate the servers.
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:32794..32797"));
        cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder));

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

    @Data
    @AllArgsConstructor
    private static class Person {
        private int age;
        private String name;
    }
}

