package cinema.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Screening {

    private final String id;
    private final Film film;
    private final Hall hall;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final List<Seat> seats;
    private final boolean is3D;

    public Screening(Film film, Hall hall, LocalDateTime start, LocalDateTime end, boolean is3D) {
        this.id    = UUID.randomUUID().toString();
        this.film  = film;
        this.hall  = hall;
        this.start = start;
        this.end   = end;
        this.is3D  = is3D;
        this.seats = new ArrayList<>();
        generateSeats();
    }

    public Screening(Film film, Hall hall, LocalDateTime start, LocalDateTime end) {
        this(film, hall, start, end, false);
    }

    // Generuje miejsca na podstawie pojemności sali.
    // Miejsca w rzędzie 1 są oznaczone jako premium.
    private void generateSeats() {
        int capacity    = hall.getCapacity();
        int rows        = Math.max(1, capacity / 10);
        int seatsPerRow = (int) Math.ceil((double) capacity / rows);
        int count = 0;
        for (int r = 1; r <= rows && count < capacity; r++) {
            for (int n = 1; n <= seatsPerRow && count < capacity; n++) {
                seats.add(new Seat(r, count + 1, r == 1));
                count++;
            }
        }
    }

    // ── Logika biznesowa ──────────────────────────────────────────────────

    /**
     * Sprawdza czy ten seans koliduje czasowo z innym seansem w tej samej sali.
     * Zwraca true jeśli zachodzi nakładanie się terminów.
     */
    public boolean isConflictWith(Screening other) {
        if (!this.hall.getId().equals(other.hall.getId())) {
            return false;
        }
        return !(this.end.compareTo(other.start) <= 0
              || this.start.compareTo(other.end) >= 0);
    }

    /**
     * Sprawdza czy na seans jest choć jedno wolne miejsce.
     */
    public boolean checkAvailability() {
        return seats.stream().anyMatch(s -> !s.isReserved());
    }

    /**
     * Rezerwuje miejsce o podanym numerze.
     * Zwraca true jeśli rezerwacja się powiodła, false jeśli miejsce zajęte lub nie istnieje.
     */
    public boolean reserveSeat(int seatNumber) {
        Optional<Seat> seat = seats.stream()
                .filter(s -> s.getNumber() == seatNumber && !s.isReserved())
                .findFirst();
        seat.ifPresent(Seat::reserve);
        return seat.isPresent();
    }

    /**
     * Zwraca miejsce o podanym numerze lub Optional.empty() jeśli nie istnieje.
     */
    public Optional<Seat> getSeat(int seatNumber) {
        return seats.stream()
                .filter(s -> s.getNumber() == seatNumber)
                .findFirst();
    }

    // ── Gettery ───────────────────────────────────────────────────────────

    public String getId()            { return id; }
    public Film getFilm()            { return film; }
    public Hall getHall()            { return hall; }
    public LocalDateTime getStart()  { return start; }
    public LocalDateTime getEnd()    { return end; }
    public List<Seat> getSeats()     { return seats; }
    public boolean is3D()            { return is3D; }

    @Override
    public String toString() {
        return "Screening{id='" + id
                + "', film='" + film.getTitle()
                + "', hall='" + hall.getName()
                + "', start=" + start
                + ", end=" + end
                + ", 3D=" + is3D + "}";
    }
}
