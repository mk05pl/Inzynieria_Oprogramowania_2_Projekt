package cinema.model;

import java.util.UUID;


public abstract class User {

    private final String id;
    private final String username;
    private final String password;

    public User(String username, String password) {
        this.id       = UUID.randomUUID().toString();
        this.username = username;
        this.password = password;
    }

    public User(String id, String username, String password) {
        this.id       = id;
        this.username = username;
        this.password = password;
    }

    /** Weryfikuje czy podane dane logowania są zgodne. */
    public boolean authenticate(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public String getId()       { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{id='" + id + "', username='" + username + "'}";
    }
}
