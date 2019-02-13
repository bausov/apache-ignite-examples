package ignite;

import ignite.model.Person;
import ignite.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

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

        while (true) {
            final Person person = new Person("name", "secondNmae");

            System.out.println("PRESS: ");
            Scanner scan = new Scanner(System.in);
            String myLine = scan.nextLine();

            if (myLine.equals("exit")) break;
            if (myLine.equals("1")) {
                for (int i = 0; i < 10; i++) {
                    System.out.println("PUT: " + repository.save((long) i, person));
                }
            }

            if (myLine.equals("2")) {
                for (int i = 0; i < 10; i++) {
                    System.out.println("GOT: " + repository.findById((long) i));
                }
            }
        }
    }
}
