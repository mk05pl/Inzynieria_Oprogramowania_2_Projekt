package cinema.model;

import java.util.UUID;

public class Ticket {

    public enum TicketStatus {
        PURCHASED,
        RESERVED,
        RETURNED
    }

    private final String id;
    private final Screening screening;
    private final Seat seat;
    private float price;
    private final User owner;
    private TicketStatus status;

    public Ticket(Screening screening, Seat seat, float price, User owner) {
        this.id        = UUID.randomUUID().toString();
        this.screening = screening;
        this.seat      = seat;
        this.price     = price;
        this.owner     = owner;
        this.status    = TicketStatus.PURCHASED;
    }

    public Ticket(Screening screening, Seat seat, float price, User owner, TicketStatus status) {
        this.id        = UUID.randomUUID().toString();
        this.screening = screening;
        this.seat      = seat;
        this.price     = price;
        this.owner     = owner;
        this.status    = status;
    }

    /**
     * Zwrot biletu – zwalnia miejsce i ustawia status RETURNED.
     * Rzuca wyjątek jeśli bilet już zwrócony.
     */
    public void refund() {
        if (status == TicketStatus.RETURNED) {
            throw new IllegalStateException("Bilet został już zwrócony.");
        }
        seat.release();
        this.status = TicketStatus.RETURNED;
    }

    public String getId()              { return id; }
    public Screening getScreening()    { return screening; }
    public Seat getSeat()              { return seat; }
    public float getPrice()            { return price; }
    public void setPrice(float price)  { this.price = price; }
    public User getOwner()             { return owner; }
    public TicketStatus getStatus()    { return status; }

    @Override
    public String toString() {
        return "Ticket{id='" + id
                + "', film='" + screening.getFilm().getTitle()
                + "', seat=" + seat
                + ", price=" + price
                + ", status=" + status + "}";
    }
}
