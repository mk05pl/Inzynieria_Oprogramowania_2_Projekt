package cinema.model;


public class Employee extends User {

    public Employee(String username, String password) {
        super(username, password);
    }

    public Employee(String id, String username, String password) {
        super(id, username, password);
    }
}
