package ignite;

import ignite.model.Person;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.WALMode;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
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

    @Bean
    public Ignite igniteInstance() {
        IgniteConfiguration cfg = new IgniteConfiguration();
//        cfg.setClientMode(true);

        CacheConfiguration ccfg = new CacheConfiguration("PersonCache");
        ccfg.setIndexedTypes(Long.class, Person.class);
        cfg.setCacheConfiguration(ccfg);

        // Ignite persistence configuration.
//        DataStorageConfiguration storageCfg = new DataStorageConfiguration();
//        storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
//        storageCfg.setWalMode(WALMode.FSYNC);
//        cfg.setDataStorageConfiguration(storageCfg);

        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        spi.setIpFinder(new TcpDiscoveryVmIpFinder(true));
        cfg.setDiscoverySpi(spi);

        return Ignition.start(cfg);
    }
}
