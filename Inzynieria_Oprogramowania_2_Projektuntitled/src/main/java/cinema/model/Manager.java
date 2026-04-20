package cinema.model;


public class Manager extends User {

    public Manager(String username, String password) {
        super(username, password);
    }

    public Manager(String id, String username, String password) {
        super(id, username, password);
    }
}
