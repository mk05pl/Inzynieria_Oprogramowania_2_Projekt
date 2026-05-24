package cinema.facade;

import cinema.builder.ScreeningBuilder;
import cinema.model.*;
import cinema.service.CinemaSystem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Wzorzec Fasada – upraszcza dostęp do złożonego podsystemu.
 * Udostępnia trzy główne operacje: zakup biletu, zwrot biletu, tworzenie seansu.
 * Klient fasady nie musi znać szczegółów CinemaSystem, Screening, Seat, Ticket.
 * Autor: Michał Kowalski
 */
public class CinemaSystemFacade {

    private final CinemaSystem system = CinemaSystem.getInstance();

    // ── Zakup biletu ──────────────────────────────────────────────────────

    /**
     * Kupuje bilet dla użytkownika na podany seans i miejsce.
     * Sprawdza dostępność, rezerwuje miejsce i tworzy bilet.
     *
     * @return Ticket jeśli zakup się powiódł, null jeśli seans nie istnieje
     *         lub miejsce jest zajęte
     */
    public Ticket buyTicket(User user, String screeningId, int seatNumber) {
        Optional<Screening> opt = system.findScreening(screeningId);
        if (opt.isEmpty()) return null;

        Screening screening = opt.get();

        if (!screening.checkAvailability()) return null;
        if (!screening.reserveSeat(seatNumber)) return null;

        Seat seat = screening.getSeat(seatNumber).orElse(null);
        if (seat == null) return null;

        Ticket ticket = new Ticket(screening, seat, 20.0f, user);

        if (user instanceof Customer customer) {
            customer.addTicket(ticket);
        }
        return ticket;
    }

    // ── Zwrot biletu ──────────────────────────────────────────────────────

    /**
     * Zwraca bilet dla użytkownika.
     * Sprawdza własność biletu, wywołuje refund() i usuwa bilet z konta klienta.
     *
     * @return true jeśli zwrot się powiódł, false jeśli bilet nie należy do użytkownika
     */
    public boolean refundTicket(User user, Ticket ticket) {
        if (!(user instanceof Customer customer)) return false;
        if (!customer.getTickets().contains(ticket)) return false;

        ticket.refund();
        customer.removeTicket(ticket);
        return true;
    }

    // ── Tworzenie seansu ──────────────────────────────────────────────────

    /**
     * Tworzy nowy seans dla menadżera używając wzorca Builder.
     * Waliduje dane i sprawdza wolność sali.
     *
     * @return nowo utworzony Screening
     * @throws IllegalArgumentException gdy brakuje wymaganego pola
     * @throws IllegalStateException    gdy sala jest zajęta
     */
    public Screening createScreening(Manager manager, Film film, Hall hall,
                                     LocalDateTime start, boolean is3D) {
        return new ScreeningBuilder()
                .setFilm(film)
                .setHall(hall)
                .setStart(start)
                .set3D(is3D)
                .setEndFromFilm()
                .build();
    }

    // ── Pomocnicze ────────────────────────────────────────────────────────

    /**
     * Zwraca listę seansów z dostępnymi miejscami.
     */
    public List<Screening> getAvailableScreenings() {
        return system.getScreenings().stream()
                .filter(Screening::checkAvailability)
                .toList();
    }

    /**
     * Zwraca wszystkie seanse.
     */
    public List<Screening> getAllScreenings() {
        return system.getScreenings();
    }
}
