package ignite.repository;

import ignite.model.Person;
import org.apache.ignite.springdata20.repository.IgniteRepository;
import org.apache.ignite.springdata20.repository.config.RepositoryConfig;

import java.util.List;

/**
 * Created by GreenNun on 2019-02-11.
 */
@RepositoryConfig(cacheName = "PersonCache")
public interface PersonRepository extends IgniteRepository<Person, Long> {
    List<Person> findAllByCounter(int counter);

    List<Person> findAllByFirstName(String firstName);
}
