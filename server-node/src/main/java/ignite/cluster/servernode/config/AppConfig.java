package ignite.cluster.servernode.config;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.WALMode;
import org.apache.ignite.spi.discovery.zk.ZookeeperDiscoverySpi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by GreenNun on 2019-02-12.
 */
@Configuration
@SuppressWarnings("all")
public class AppConfig {

    @Bean
    public Ignite igniteInstance() {
        final String instanceName = "ignite-server-0";

        final IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setIgniteInstanceName(instanceName);
        cfg.setPeerClassLoadingEnabled(true);

        // Ignite persistence configuration.
        final DataStorageConfiguration storageCfg = new DataStorageConfiguration();
        storageCfg.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
        storageCfg.setWalMode(WALMode.FSYNC);
        storageCfg.setWalCompactionEnabled(true);
        storageCfg.setStoragePath("./ignite/" + instanceName + "/storage");
        storageCfg.setWalPath("./ignite/" + instanceName + "/wal");
        storageCfg.setWalArchivePath("./ignite/" + instanceName + "/walarchive");
        cfg.setDataStorageConfiguration(storageCfg);

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
