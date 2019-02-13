package ignite.model;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * Created by GreenNun on 2019-02-11.
 */
public class Person {
    @QuerySqlField(index = true)
    private String firstName;
    @QuerySqlField(index = true)
    private String secondName;
    private int counter;

    public Person(String firstName, String secondName, int counter) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.counter = counter;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public String toString() {
        return "Person{" +
                "firstName= '" + firstName + '\'' +
                ", secondName= '" + secondName + '\'' +
                ", counter= " + counter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Person person = (Person) o;

        if (firstName != null ? !firstName.equals(person.firstName) : person.firstName != null)
            return false;

        return secondName != null ? secondName.equals(person.secondName) : person.secondName == null;

    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (secondName != null ? secondName.hashCode() : 0);
        return result;
    }
}
