package ignite.service;

import ignite.model.Person;
import ignite.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * Created by GreenNun on 2019-02-13.
 */
@Service
public class RepositoryService {
    private final PersonRepository repository;

    @Autowired
    public RepositoryService(PersonRepository repository) {
        this.repository = repository;
    }

    @Transactional(value = "pessimisticTransactionManager", isolation = Isolation.SERIALIZABLE)
    public void transactionP(int add, String thread, long delay) {
        tx(add, thread, delay);
    }

    @Transactional(value = "optimisticTransactionManager", isolation = Isolation.SERIALIZABLE)
    public void transactionO(int add, String thread, long delay) {
        tx(add, thread, delay);
    }

    public void read() {
        System.out.println("BY COUNTER: " + repository.findAllByCounter(0));
    }

    private void tx(int add, String thread, long delay) {
        for (int i = 0; i < 10; i++) {
            final Person person = repository.findById((long) i).get();
            person.setSecondName(thread);
            person.setCounter(person.getCounter() + add);
            System.out.println("PUT (" + thread + "): " + repository.save((long) i, person));
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @PostConstruct
    private void init() {
        repository.findAll();
    }
}
