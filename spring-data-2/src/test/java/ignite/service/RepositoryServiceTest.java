package ignite.service;

import ignite.model.Person;
import ignite.repository.PersonRepository;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.*;
import org.apache.ignite.cache.eviction.fifo.FifoEvictionPolicyFactory;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.NearCacheConfiguration;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.spring.SpringTransactionManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Optional;

/**
 * Created by GreenNun on 2019-02-13.
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class RepositoryServiceTest {
    private static PersonRepository repository;
    private static RepositoryService service;

    @BeforeClass
    public static void setUp() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(RepositoryServiceTest.AppConfig.class);
        context.register(PersonRepository.class);
        context.refresh();


        repository = context.getBean(PersonRepository.class);
        service = new RepositoryService(repository);
    }

    @Test
    public void transactionP() {
        System.out.println(service);
    }

    @Test
    public void transactionO() {
        repository.save(1L, new Person("name", "sname", 22));
        final Optional<Person> person = repository.findById(1L);
        System.out.println("PERSON: " + person.get().toString());
    }

    @TestConfiguration
    @EnableIgniteRepositories(basePackageClasses = PersonRepository.class)
    @ComponentScan(basePackages = "ignite.repository.*")
    @SuppressWarnings("all")
    public static class AppConfig {
        private final String instanceName = "ignite-client";

        @Bean
        public IgniteConfiguration igniteConfiguration() {
            IgniteConfiguration cfg = new IgniteConfiguration();
            cfg.setClientMode(false);
            cfg.setIgniteInstanceName(instanceName);
            cfg.setPeerClassLoadingEnabled(true);

            NearCacheConfiguration<Long, Person> nearCfg = new NearCacheConfiguration<>();
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

            return cfg;
        }

        @Bean
        public Ignite igniteInstance() {
            final Ignite ignite = Ignition.start(igniteConfiguration());
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

        @Bean
        public PlatformTransactionManager transactionManager() {
            return new ChainedTransactionManager(new PlatformTransactionManager[]{igniteTransactionManagerOpt(), igniteTransactionManagerPes()});
        }
    }
}