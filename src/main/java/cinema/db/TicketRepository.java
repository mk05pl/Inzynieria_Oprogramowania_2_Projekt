package cinema.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class TicketRepository {

    private final Connection conn;

    public TicketRepository() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ── Dostępne miejsca ─────────────────────────────────────────────────

    /**
     * Zwraca listę wolnych miejsc na podany seans.
     */
    public List<SeatRecord> getAvailableSeats(String screeningId) throws SQLException {
        String sql = """
                SELECT id, row_number, seat_number, is_premium
                FROM seats
                WHERE screening_id = ? AND is_reserved = 0
                ORDER BY row_number, seat_number
                """;
        List<SeatRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, screeningId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SeatRecord(
                            rs.getString("id"),
                            rs.getInt("row_number"),
                            rs.getInt("seat_number"),
                            rs.getBoolean("is_premium")));
                }
            }
        }
        return list;
    }

    // ── Zakup biletu ──────────────────────────────────────────────────────

    /**
     * Kupuje bilet – rezerwuje miejsce i tworzy rekord biletu w transakcji.
     *
     * @return id nowego biletu lub null gdy miejsce zajęte / nie istnieje
     */
    public String buyTicket(String userId, String screeningId,
                             int seatNumber, float price) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // Zablokuj wiersz miejsca (SELECT FOR UPDATE)
            String findSeat = """
                    SELECT id FROM seats
                    WHERE screening_id = ?
                      AND seat_number  = ?
                      AND is_reserved  = 0
                    LIMIT 1
                    FOR UPDATE
                    """;
            String seatId;
            try (PreparedStatement ps = conn.prepareStatement(findSeat)) {
                ps.setString(1, screeningId);
                ps.setInt(2, seatNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return null;
                    }
                    seatId = rs.getString("id");
                }
            }

            // Zarezerwuj miejsce
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE seats SET is_reserved = 1 WHERE id = ?")) {
                ps.setString(1, seatId);
                ps.executeUpdate();
            }

            // Utwórz bilet
            String ticketId = UUID.randomUUID().toString();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO tickets (id, screening_id, seat_id, owner_id, price, status) "
                    + "VALUES (?, ?, ?, ?, ?, 'PURCHASED')")) {
                ps.setString(1, ticketId);
                ps.setString(2, screeningId);
                ps.setString(3, seatId);
                ps.setString(4, userId);
                ps.setFloat(5, price);
                ps.executeUpdate();
            }

            conn.commit();
            return ticketId;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ── Zwrot biletu ──────────────────────────────────────────────────────

    /**
     * Zwraca bilet – zwalnia miejsce i ustawia status RETURNED.
     *
     * @return true jeśli zwrot się powiódł
     */
    public boolean refundTicket(String userId, String ticketId) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // Sprawdź własność i pobierz seat_id
            String findTicket = """
                    SELECT t.seat_id FROM tickets t
                    WHERE t.id = ? AND t.owner_id = ? AND t.status != 'RETURNED'
                    FOR UPDATE
                    """;
            String seatId;
            try (PreparedStatement ps = conn.prepareStatement(findTicket)) {
                ps.setString(1, ticketId);
                ps.setString(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    seatId = rs.getString("seat_id");
                }
            }

            // Zwolnij miejsce
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE seats SET is_reserved = 0 WHERE id = ?")) {
                ps.setString(1, seatId);
                ps.executeUpdate();
            }

            // Zmień status biletu
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE tickets SET status = 'RETURNED' WHERE id = ?")) {
                ps.setString(1, ticketId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ── DTO ───────────────────────────────────────────────────────────────

    public record SeatRecord(String seatId, int row, int seatNumber, boolean isPremium) {}
}
