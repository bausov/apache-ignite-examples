package ignite;

import ignite.model.Person;
import ignite.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringData2Application implements CommandLineRunner {
    private final PersonRepository repository;

    @Autowired
    public SpringData2Application(PersonRepository repository) {
        this.repository = repository;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringData2Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final Person person = new Person("name", "secondNmae");

        for (int i = 0; i < 10; i++) {
            System.out.println("PUT: " + repository.save((long) i, person));
        }

        for (int i = 0; i < 10; i++) {
            System.out.println("GOT: " + repository.findById((long) i));
        }
    }
}
