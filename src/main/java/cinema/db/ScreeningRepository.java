package cinema.db;

import cinema.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repozytorium seansów – operacje CRUD przez JDBC.
 * Autor: Krzysztof Wysocki
 */
public class ScreeningRepository {

    private final Connection conn;

    public ScreeningRepository() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ── Odczyt ───────────────────────────────────────────────────────────

    /**
     * Zwraca wszystkie seanse z bazy wraz z filmem i salą.
     */
    public List<ScreeningRecord> findAll() throws SQLException {
        String sql = """
                SELECT s.id, s.start_time, s.end_time, s.is_3d,
                       f.id AS film_id, f.title, f.duration_minutes,
                       h.id AS hall_id, h.name AS hall_name, h.capacity
                FROM screenings s
                JOIN films f ON f.id = s.film_id
                JOIN halls  h ON h.id = s.hall_id
                ORDER BY s.start_time
                """;
        List<ScreeningRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Zwraca seanse z przyszłą datą i przynajmniej jednym wolnym miejscem.
     */
    public List<ScreeningRecord> findAvailable() throws SQLException {
        String sql = """
                SELECT s.id, s.start_time, s.end_time, s.is_3d,
                       f.id AS film_id, f.title, f.duration_minutes,
                       h.id AS hall_id, h.name AS hall_name, h.capacity
                FROM screenings s
                JOIN films f ON f.id = s.film_id
                JOIN halls  h ON h.id = s.hall_id
                WHERE s.start_time > NOW()
                  AND EXISTS (
                      SELECT 1 FROM seats se
                      WHERE se.screening_id = s.id AND se.is_reserved = 0
                  )
                ORDER BY s.start_time
                """;
        List<ScreeningRecord> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Sprawdza czy sala jest wolna w podanym przedziale – używane przez Builder.
     */
    public boolean isHallAvailable(String hallId, LocalDateTime start, LocalDateTime end)
            throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM screenings
                WHERE hall_id = ?
                  AND start_time < ?
                  AND end_time   > ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hallId);
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    // ── Zapis ────────────────────────────────────────────────────────────

    /**
     * Zapisuje nowy seans i generuje miejsca w jednej transakcji.
     *
     * @return id nowo utworzonego seansu
     */
    public String save(Film film, Hall hall, LocalDateTime start, LocalDateTime end,
                       boolean is3D) throws SQLException {
        String screeningId = UUID.randomUUID().toString();
        conn.setAutoCommit(false);
        try {
            insertScreening(screeningId, film.getId(), hall.getId(), start, end, is3D);
            generateSeats(screeningId, hall.getCapacity());
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
        return screeningId;
    }

    private void insertScreening(String id, String filmId, String hallId,
                                  LocalDateTime start, LocalDateTime end,
                                  boolean is3D) throws SQLException {
        String sql = """
                INSERT INTO screenings (id, film_id, hall_id, start_time, end_time, is_3d)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, filmId);
            ps.setString(3, hallId);
            ps.setTimestamp(4, Timestamp.valueOf(start));
            ps.setTimestamp(5, Timestamp.valueOf(end));
            ps.setBoolean(6, is3D);
            ps.executeUpdate();
        }
    }

    private void generateSeats(String screeningId, int capacity) throws SQLException {
        String sql = """
                INSERT INTO seats (id, screening_id, row_number, seat_number, is_premium, is_reserved)
                VALUES (?, ?, ?, ?, ?, 0)
                """;
        int rows = Math.max(1, capacity / 10);
        int seatsPerRow = (int) Math.ceil((double) capacity / rows);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int count = 0;
            for (int r = 1; r <= rows && count < capacity; r++) {
                for (int n = 1; n <= seatsPerRow && count < capacity; n++) {
                    ps.setString(1, UUID.randomUUID().toString());
                    ps.setString(2, screeningId);
                    ps.setInt(3, r);
                    ps.setInt(4, n);
                    ps.setBoolean(5, r == 1);
                    ps.addBatch();
                    count++;
                }
            }
            ps.executeBatch();
        }
    }

    // ── Mapper ───────────────────────────────────────────────────────────

    private ScreeningRecord mapRow(ResultSet rs) throws SQLException {
        Film film = new Film(
                rs.getString("film_id"),
                rs.getString("title"),
                rs.getInt("duration_minutes"));
        Hall hall = new Hall(
                rs.getString("hall_id"),
                rs.getString("hall_name"),
                rs.getInt("capacity"));
        return new ScreeningRecord(
                rs.getString("id"),
                film,
                hall,
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime(),
                rs.getBoolean("is_3d"));
    }

    // ── DTO ──────────────────────────────────────────────────────────────

    /**
     * Prosta struktura danych reprezentująca seans odczytany z bazy.
     */
    public record ScreeningRecord(
            String id,
            Film film,
            Hall hall,
            LocalDateTime start,
            LocalDateTime end,
            boolean is3D) {}
}
