package ignite;

import ignite.model.Person;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.zk.ZookeeperDiscoverySpi;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by GreenNun on 2019-02-11.
 */
@Configuration
@EnableIgniteRepositories
@SuppressWarnings("all")
public class AppConfig {
//        zkDiscoSpi.setZkRootPath(""); couldn't be empty
//        ccfg.setAffinity(new TestAffinityFunction(partitionsNumber, backupsNumber));
//        cfg.setConsistentId()

    @Bean
    public Ignite igniteInstance() {
        final String instanceName = "ignite-client";

        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
        cfg.setIgniteInstanceName(instanceName);
        cfg.setPeerClassLoadingEnabled(true);

        CacheConfiguration ccfg = new CacheConfiguration("PersonCache");
        ccfg.setIndexedTypes(Long.class, Person.class);
        cfg.setCacheConfiguration(ccfg);

        // Ignite persistence configuration.
//        DataStorageConfiguration storageCfg = new DataStorageConfiguration();
//        storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
//        storageCfg.setWalMode(WALMode.FSYNC);
//        cfg.setDataStorageConfiguration(storageCfg);

        ZookeeperDiscoverySpi zkDiscoSpi = new ZookeeperDiscoverySpi();
        zkDiscoSpi.setZkConnectionString("localhost:2181");
        zkDiscoSpi.setSessionTimeout(30_000);
        zkDiscoSpi.setJoinTimeout(10_000);
        cfg.setDiscoverySpi(zkDiscoSpi);

        // Connect to the cluster.
        final Ignite ignite = Ignition.start(cfg);

        // Activate the cluster. Automatic topology initialization occurs
        // only if you manually activate the cluster for the very first time.
        ignite.cluster().active(true);

        return ignite;
    }
}
