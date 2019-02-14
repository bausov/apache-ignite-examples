package ignite;

import ignite.model.Person;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.*;
import org.apache.ignite.cache.eviction.fifo.FifoEvictionPolicyFactory;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.NearCacheConfiguration;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.apache.ignite.spi.discovery.zk.ZookeeperDiscoverySpi;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.spring.SpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by GreenNun on 2019-02-11.
 */
@Configuration
@EnableIgniteRepositories
@SuppressWarnings("all")
public class AppConfig {
    private final String instanceName = "ignite-client";
//        zkDiscoSpi.setZkRootPath(""); couldn't be empty
//        ccfg.setAffinity(new TestAffinityFunction(partitionsNumber, backupsNumber));
//        cfg.setConsistentId()
    @Bean
    public IgniteConfiguration igniteConfiguration(@Autowired DiscoverySpi discoverySpi) {

        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
        cfg.setIgniteInstanceName(instanceName);
        cfg.setPeerClassLoadingEnabled(true);

        NearCacheConfiguration<Long, Person> nearCfg = new NearCacheConfiguration<>();
//        nearCfg.setNearEvictionPolicy(new LruEvictionPolicy<>(10_000)); // deprecated
        nearCfg.setNearEvictionPolicyFactory(new FifoEvictionPolicyFactory<>(10_000));
        nearCfg.setNearStartSize(1_000);

        CacheConfiguration ccfg = new CacheConfiguration("PersonCache");
        ccfg.setNearConfiguration(nearCfg);
        ccfg.setIndexedTypes(Long.class, Person.class);
        ccfg.setGroupName("group1");
        ccfg.setCacheMode(CacheMode.PARTITIONED);
        ccfg.setPartitionLossPolicy(PartitionLossPolicy.READ_WRITE_SAFE);
        ccfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        ccfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
        ccfg.setBackups(2);
        ccfg.setRebalanceMode(CacheRebalanceMode.SYNC);
        cfg.setCacheConfiguration(ccfg);


        cfg.setDiscoverySpi(discoverySpi);

        return cfg;
    }

    @Bean
    @Profile("docker")
    public DiscoverySpi discoverySpiDocker() {
        ZookeeperDiscoverySpi zkDiscoSpi = new ZookeeperDiscoverySpi();
        zkDiscoSpi.setZkConnectionString("zookeeper:2181");
        zkDiscoSpi.setSessionTimeout(30_000);
        zkDiscoSpi.setJoinTimeout(10_000);

        return zkDiscoSpi;
    }

    @Bean
    public DiscoverySpi discoverySpi() {
        ZookeeperDiscoverySpi zkDiscoSpi = new ZookeeperDiscoverySpi();
        zkDiscoSpi.setZkConnectionString("localhost:2181");
        zkDiscoSpi.setSessionTimeout(30_000);
        zkDiscoSpi.setJoinTimeout(10_000);

        return zkDiscoSpi;
    }

    @Bean
    public Ignite igniteInstance(@Autowired IgniteConfiguration igniteConfiguration) {
        // Connect to the cluster.
        final Ignite ignite = Ignition.start(igniteConfiguration);

        // Activate the cluster. Automatic topology initialization occurs
        // only if you manually activate the cluster for the very first time.
        ignite.cluster().active(true);

//        ignite.transactions().txStart(null, null);

        return ignite;
    }

    @Bean("optimisticTransactionManager")
    public SpringTransactionManager igniteTransactionManagerOpt() {
        final SpringTransactionManager transactionManager = new SpringTransactionManager();
        transactionManager.setIgniteInstanceName(instanceName);
        transactionManager.setTransactionConcurrency(TransactionConcurrency.OPTIMISTIC);

        return transactionManager;
    }

    @Bean("pessimisticTransactionManager")
    public SpringTransactionManager igniteTransactionManagerPes() {
        final SpringTransactionManager transactionManager = new SpringTransactionManager();
        transactionManager.setIgniteInstanceName(instanceName);
        transactionManager.setTransactionConcurrency(TransactionConcurrency.PESSIMISTIC);

        return transactionManager;
    }
}
