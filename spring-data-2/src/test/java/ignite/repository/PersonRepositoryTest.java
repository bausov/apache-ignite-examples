package ignite.repository;

import ignite.model.Person;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Created by GreenNun on 2019-02-18.
 */
@RunWith(SpringRunner.class)
//@SpringBootTest
@ContextConfiguration(classes = PersonRepositoryTest.AppConfig.class)
public class PersonRepositoryTest {
    @Autowired
    private PersonRepository repository;

    @Test
    public void test() {
        repository.save(1L, new Person("name", "second", 0));
        repository.save(2L, new Person("name", "second", 1));
        repository.save(3L, new Person("name0", "second", 0));

        List<Person> allByCounter = repository.findAllByCounter(0);
        System.out.println(allByCounter);

        allByCounter = repository.findAllByFirstName("name");
        System.out.println(allByCounter);
    }

    @Configuration
    @EnableIgniteRepositories("ignite.repository")
    @ComponentScan(basePackages = "ignite.model")
    public static class AppConfig {
        private final String instanceName = "ignite-client-test";

        @Autowired
        private PersonRepository repository;

        @Bean("igniteCfg")
        public IgniteConfiguration igniteConfiguration() {

            IgniteConfiguration cfg = new IgniteConfiguration();
            cfg.setClientMode(false);
            cfg.setIgniteInstanceName(instanceName);
            cfg.setPeerClassLoadingEnabled(true);

            CacheConfiguration ccfg = new CacheConfiguration("PersonCache");
            ccfg.setIndexedTypes(Long.class, Person.class);
            cfg.setCacheConfiguration(ccfg);

            return cfg;
        }

        @Bean("igniteInstance")
        public Ignite igniteInstance(@Autowired IgniteConfiguration igniteConfiguration) {
            final Ignite ignite = Ignition.start(igniteConfiguration);
//            ignite.cluster().active(true);
            return ignite;
        }
    }

}