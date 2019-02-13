package ignite;

import ignite.model.Person;
import ignite.repository.PersonRepository;
import ignite.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class SpringData2Application implements CommandLineRunner {
    private final PersonRepository repository;
    private final RepositoryService service;

    @Autowired
    public SpringData2Application(PersonRepository repository, RepositoryService service) {
        this.repository = repository;
        this.service = service;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringData2Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        while (true) {
            final Person person = new Person("name", "secondNmae", 0);

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

            if (myLine.equals("3")) {
                System.out.println("PESSIMISTIC x 2");
                new Thread(() -> {
                    service.transactionP(1, "1", 1000);
                }).start();
                new Thread(() -> {
                    service.transactionP(-1, "2", 500);
                }).start();
            }

            if (myLine.equals("4")) {
                System.out.println("OPTIMISTIC x 2");
                new Thread(() -> {
                    service.transactionO(1, "1", 1000);
                }).start();
                new Thread(() -> {
                    service.transactionO(-1, "2", 500);
                }).start();
            }

            if (myLine.equals("5")) {
                System.out.println("MULTI");
                new Thread(() -> {
                    service.transactionP(1, "1", 1000);
                }).start();
                new Thread(() -> {
                    service.transactionO(-1, "2", 500);
                }).start();
            }

            if (myLine.equals("6")) {
                System.out.println("MULTI");
                new Thread(() -> {
                    service.transactionO(1, "1", 1000);
                }).start();
                new Thread(() -> {
                    service.transactionP(-1, "2", 500);
                }).start();
            }
        }
    }
}
