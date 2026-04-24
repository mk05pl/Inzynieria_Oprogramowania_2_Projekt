package cinema.model;

import java.util.ArrayList;
import java.util.List;


public class Customer extends User {

    private final List<Ticket> tickets = new ArrayList<>();

    public Customer(String username, String password) {
        super(username, password);
    }

    public Customer(String id, String username, String password) {
        super(id, username, password);
    }

    public List<Ticket> getTickets()          { return tickets; }
    public void addTicket(Ticket t)           { tickets.add(t); }
    public boolean removeTicket(Ticket t)     { return tickets.remove(t); }
}
