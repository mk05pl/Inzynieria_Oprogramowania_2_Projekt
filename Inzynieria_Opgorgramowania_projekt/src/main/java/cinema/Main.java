package cinema;

import cinema.model.Customer;
import cinema.model.Employee;
import cinema.model.Film;
import cinema.model.Hall;
import cinema.model.Manager;
import cinema.model.Screening;
import cinema.model.Seat;
import cinema.model.Ticket;
import cinema.service.CinemaSystem;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        CinemaSystem system = CinemaSystem.getInstance();

        Manager  manager  = new Manager("admin", "admin123");
        Employee employee = new Employee("kasjer1", "pass1");
        Customer customer = new Customer("jan.kowalski", "haslo1");

        system.addUser(manager);
        system.addUser(employee);
        system.addUser(customer);

        System.out.println("=== Użytkownicy ===");
        system.getUsers().forEach(System.out::println);

        System.out.println("\n=== Logowanie ===");
        system.authenticate("jan.kowalski", "haslo1")
                .ifPresentOrElse(
                        u  -> System.out.println("OK: " + u),
                        () -> System.out.println("Błędne dane.")
                );
        system.authenticate("jan.kowalski", "zle")
                .ifPresentOrElse(
                        u  -> System.out.println("OK: " + u),
                        () -> System.out.println("FAIL: błędne hasło.")
                );

        Film film1 = new Film("Inception", 148);
        Film film2 = new Film("Interstellar", 169);
        Hall hall1 = new Hall("H1", "Sala 1", 30);
        Hall hall2 = new Hall("H2", "Sala 2", 50);

        LocalDateTime start1 = LocalDateTime.of(2026, 5, 10, 18, 0);
        Screening s1 = new Screening(film1, hall1, start1, start1.plusMinutes(148));

        LocalDateTime start2 = LocalDateTime.of(2026, 5, 10, 18, 0);
        Screening s2 = new Screening(film2, hall2, start2, start2.plusMinutes(169));

        system.addScreening(s1);
        system.addScreening(s2);

        System.out.println("\n=== Seanse ===");
        system.getScreenings().forEach(System.out::println);

        System.out.println("\n=== Dostępność sali ===");
        System.out.println("Sala 1 wolna 19:00-21:00? "
                + system.isHallAvailable(hall1,
                LocalDateTime.of(2026, 5, 10, 19, 0),
                LocalDateTime.of(2026, 5, 10, 21, 0))); // false – koliduje z s1

        System.out.println("\n=== Konflikt terminów ===");
        Screening konflikt = new Screening(film2, hall1,
                LocalDateTime.of(2026, 5, 10, 19, 0),
                LocalDateTime.of(2026, 5, 10, 21, 0));

        System.out.println("s1 koliduje z seansem o 19:00 w Sali 1? " + s1.isConflictWith(konflikt)); // true
        System.out.println("s2 koliduje z seansem o 19:00 w Sali 1? " + s2.isConflictWith(konflikt)); // false – inna sala

        try {
            system.addScreening(konflikt);
        } catch (IllegalStateException e) {
            System.out.println("Zablokowano dodanie: " + e.getMessage());
        }

        System.out.println("\n=== Zakup biletu ===");
        System.out.println("Dostępność s1: " + s1.checkAvailability());
        System.out.println("Rezerwacja miejsca 5: " + s1.reserveSeat(5));

        Seat seat = s1.getSeat(5).orElseThrow();
        Ticket bilet = new Ticket(s1, seat, 20f, customer);
        customer.addTicket(bilet);
        System.out.println("Bilet: " + bilet);
        System.out.println("Bilety klienta: " + customer.getTickets().size());

        System.out.println("\n=== Zwrot biletu ===");
        bilet.refund();
        System.out.println("Status: " + bilet.getStatus());
        System.out.println("Miejsce 5 wolne: " + !seat.isReserved());
    }
}
