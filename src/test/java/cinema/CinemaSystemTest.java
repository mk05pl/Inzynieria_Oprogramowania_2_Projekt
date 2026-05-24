package cinema;

import cinema.model.*;
import cinema.service.CinemaSystem;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CinemaSystemTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 5, 10, 18, 0);
    private static final LocalDateTime END   = START.plusMinutes(120);

    private Film film;
    private Hall hall;

    @BeforeEach
    void setUp() {
        CinemaSystem.resetInstance();
        film = new Film("Inception", 120);
        hall = new Hall("H1", "Sala 1", 20);
    }

    // ── Film () ─────────────────────────────────────────────────────

    @Test @Order(1)
    @DisplayName("Film: przechowuje tytuł i czas trwania")
    void testFilm() {
        assertEquals("Inception", film.getTitle());
        assertEquals(120, film.getDurationMinutes());
        assertNotNull(film.getId());
    }

    // ── Hall () ─────────────────────────────────────────────────────

    @Test @Order(2)
    @DisplayName("Hall: przechowuje id, nazwę i pojemność")
    void testHall() {
        assertEquals("H1", hall.getId());
        assertEquals("Sala 1", hall.getName());
        assertEquals(20, hall.getCapacity());
    }

    // ── Seat () ─────────────────────────────────────────────────────

    @Test @Order(3)
    @DisplayName("Seat: nowe miejsce jest wolne")
    void testSeatInitiallyFree() {
        assertFalse(new Seat(1, 1).isReserved());
    }

    @Test @Order(4)
    @DisplayName("Seat: reserve() rezerwuje miejsce")
    void testSeatReserve() {
        Seat s = new Seat(1, 1);
        s.reserve();
        assertTrue(s.isReserved());
    }

    @Test @Order(5)
    @DisplayName("Seat: release() zwalnia miejsce")
    void testSeatRelease() {
        Seat s = new Seat(1, 1);
        s.reserve();
        s.release();
        assertFalse(s.isReserved());
    }

    @Test @Order(6)
    @DisplayName("Seat: reserve() na zajętym rzuca wyjątek")
    void testSeatDoubleReserveThrows() {
        Seat s = new Seat(1, 1);
        s.reserve();
        assertThrows(IllegalStateException.class, s::reserve);
    }

    // ── User – dziedziczenie OOP () ─────────────────────────────────

    @Test @Order(7)
    @DisplayName("User: Customer, Employee, Manager dziedziczą po User")
    void testUserInheritance() {
        assertInstanceOf(User.class, new Customer("a", "b"));
        assertInstanceOf(User.class, new Employee("c", "d"));
        assertInstanceOf(User.class, new Manager("e", "f"));
    }

    @Test @Order(8)
    @DisplayName("Customer: addTicket i getTickets działają poprawnie")
    void testCustomerTickets() {
        Screening s = new Screening(film, hall, START, END);
        Customer  c = new Customer("jan", "haslo");
        s.reserveSeat(1);
        Ticket t = new Ticket(s, s.getSeat(1).orElseThrow(), 20f, c);
        c.addTicket(t);
        assertEquals(1, c.getTickets().size());
        assertTrue(c.getTickets().contains(t));
    }

    // ── Screening () ────────────────────────────────────────────────

    @Test @Order(9)
    @DisplayName("Screening: generuje miejsca zgodnie z pojemnością sali")
    void testSeatsGenerated() {
        assertEquals(20, new Screening(film, hall, START, END).getSeats().size());
    }

    @Test @Order(10)
    @DisplayName("Screening: rząd 1 to miejsca premium")
    void testPremiumSeats() {
        Screening s = new Screening(film, hall, START, END);
        assertTrue(s.getSeat(1).orElseThrow().isPremium());
    }

    @Test @Order(11)
    @DisplayName("Screening: checkAvailability – wszystkie wolne na starcie")
    void testCheckAvailabilityInitial() {
        assertTrue(new Screening(film, hall, START, END).checkAvailability());
    }

    @Test @Order(12)
    @DisplayName("Screening: reserveSeat – rezerwuje wolne miejsce")
    void testReserveSeat() {
        Screening s = new Screening(film, hall, START, END);
        assertTrue(s.reserveSeat(1));
        assertTrue(s.getSeat(1).orElseThrow().isReserved());
    }

    @Test @Order(13)
    @DisplayName("Screening: reserveSeat – false gdy miejsce zajęte")
    void testReserveSeatAlreadyTaken() {
        Screening s = new Screening(film, hall, START, END);
        s.reserveSeat(1);
        assertFalse(s.reserveSeat(1));
    }

    @Test @Order(14)
    @DisplayName("Screening: getSeat – zwraca Optional.empty() dla nieistniejącego")
    void testGetSeatMissing() {
        assertTrue(new Screening(film, hall, START, END).getSeat(999).isEmpty());
    }

    @Test @Order(15)
    @DisplayName("isConflictWith: nakładające się terminy w tej samej sali → true")
    void testConflictSameHallOverlap() {
        Screening s1 = new Screening(film, hall, START, END);
        Screening s2 = new Screening(film, hall, START.plusMinutes(30), END.plusMinutes(30));
        assertTrue(s1.isConflictWith(s2));
    }

    @Test @Order(16)
    @DisplayName("isConflictWith: różne sale → false")
    void testNoConflictDifferentHall() {
        Hall hall2 = new Hall("H2", "Sala 2", 50);
        Screening s1 = new Screening(film, hall,  START, END);
        Screening s2 = new Screening(film, hall2, START, END);
        assertFalse(s1.isConflictWith(s2));
    }

    @Test @Order(17)
    @DisplayName("isConflictWith: seanse jeden po drugim → false")
    void testNoConflictSequential() {
        Screening s1 = new Screening(film, hall, START, END);
        Screening s2 = new Screening(film, hall, END, END.plusMinutes(120));
        assertFalse(s1.isConflictWith(s2));
    }

    // ── Ticket () ───────────────────────────────────────────────────

    @Test @Order(18)
    @DisplayName("Ticket: status PURCHASED po zakupie")
    void testTicketPurchased() {
        Screening s = new Screening(film, hall, START, END);
        s.reserveSeat(1);
        Ticket t = new Ticket(s, s.getSeat(1).orElseThrow(), 20f, new Customer("jan", "x"));
        assertEquals(Ticket.TicketStatus.PURCHASED, t.getStatus());
    }

    @Test @Order(19)
    @DisplayName("Ticket: refund() ustawia RETURNED i zwalnia miejsce")
    void testTicketRefund() {
        Screening s = new Screening(film, hall, START, END);
        s.reserveSeat(2);
        Seat seat = s.getSeat(2).orElseThrow();
        Ticket t  = new Ticket(s, seat, 20f, new Customer("anna", "x"));
        t.refund();
        assertEquals(Ticket.TicketStatus.RETURNED, t.getStatus());
        assertFalse(seat.isReserved());
    }

    @Test @Order(20)
    @DisplayName("Ticket: podwójny refund() rzuca wyjątek")
    void testTicketDoubleRefund() {
        Screening s = new Screening(film, hall, START, END);
        s.reserveSeat(3);
        Ticket t = new Ticket(s, s.getSeat(3).orElseThrow(), 20f, new Customer("p", "x"));
        t.refund();
        assertThrows(IllegalStateException.class, t::refund);
    }

    // ── CinemaSystem – Singleton () ──────────────────────────────

    @Test @Order(21)
    @DisplayName("CinemaSystem: getInstance zwraca ten sam obiekt")
    void testSingleton() {
        assertSame(CinemaSystem.getInstance(), CinemaSystem.getInstance());
    }

    @Test @Order(22)
    @DisplayName("CinemaSystem: addScreening dodaje seans")
    void testAddScreening() {
        CinemaSystem sys = CinemaSystem.getInstance();
        sys.addScreening(new Screening(film, hall, START, END));
        assertEquals(1, sys.getScreenings().size());
    }

    // ── isHallAvailable () ───────────────────────────────────────

    @Test @Order(23)
    @DisplayName("isHallAvailable: sala wolna gdy brak seansów")
    void testHallAvailableEmpty() {
        assertTrue(CinemaSystem.getInstance().isHallAvailable(hall, START, END));
    }

    @Test @Order(24)
    @DisplayName("isHallAvailable: sala zajęta gdy seans koliduje")
    void testHallUnavailableOnConflict() {
        CinemaSystem sys = CinemaSystem.getInstance();
        sys.addScreening(new Screening(film, hall, START, END));
        assertFalse(sys.isHallAvailable(hall, START.plusMinutes(30), END.plusMinutes(30)));
    }

    @Test @Order(25)
    @DisplayName("isHallAvailable: sala wolna dla innej sali")
    void testHallAvailableDifferentHall() {
        CinemaSystem sys  = CinemaSystem.getInstance();
        Hall hall2 = new Hall("H2", "Sala 2", 50);
        sys.addScreening(new Screening(film, hall, START, END));
        assertTrue(sys.isHallAvailable(hall2, START, END));
    }

    @Test @Order(26)
    @DisplayName("CinemaSystem: addScreening rzuca wyjątek przy konflikcie")
    void testAddScreeningConflictThrows() {
        CinemaSystem sys = CinemaSystem.getInstance();
        sys.addScreening(new Screening(film, hall, START, END));
        assertThrows(IllegalStateException.class, () ->
                sys.addScreening(new Screening(film, hall, START.plusMinutes(30), END.plusMinutes(30)))
        );
    }

    // ── authenticate () ──────────────────────────────────────────

    @Test @Order(27)
    @DisplayName("authenticate: poprawne dane zwracają użytkownika")
    void testAuthenticateOk() {
        CinemaSystem sys = CinemaSystem.getInstance();
        sys.addUser(new Customer("jan", "haslo1"));
        assertTrue(sys.authenticate("jan", "haslo1").isPresent());
    }

    @Test @Order(28)
    @DisplayName("authenticate: błędne hasło zwraca empty")
    void testAuthenticateFail() {
        CinemaSystem sys = CinemaSystem.getInstance();
        sys.addUser(new Customer("jan", "haslo1"));
        assertTrue(sys.authenticate("jan", "zle").isEmpty());
    }

    @Test @Order(29)
    @DisplayName("authenticate: nieznany użytkownik zwraca empty")
    void testAuthenticateUnknownUser() {
        assertTrue(CinemaSystem.getInstance().authenticate("ktos", "haslo").isEmpty());
    }
}
