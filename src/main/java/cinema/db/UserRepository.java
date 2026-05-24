package cinema.db;

import cinema.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repozytorium użytkowników – uwierzytelnianie, wyszukiwanie, rejestracja.
 * Autor: Krzysztof Wysocki
 */
public class UserRepository {

    private final Connection conn;

    public UserRepository() {
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    // ── Odczyt ───────────────────────────────────────────────────────────

    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, username, password, role FROM users ORDER BY role, username";
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<User> findById(String id) throws SQLException {
        String sql = "SELECT id, username, password, role FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Uwierzytelnia użytkownika.
     * UWAGA: produkcja wymaga BCrypt – tu plain text (demo).
     */
    public Optional<User> authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                if (!password.equals(rs.getString("password"))) return Optional.empty();
                return Optional.of(mapRow(rs));
            }
        }
    }

    /**
     * Rejestruje nowego użytkownika. Zwraca wygenerowane id.
     */
    public String register(String username, String password, String role) throws SQLException {
        String id  = UUID.randomUUID().toString();
        String sql = "INSERT INTO users (id, username, password, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, role);
            ps.executeUpdate();
        }
        return id;
    }

    // ── Mapper ───────────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        String id       = rs.getString("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        return switch (rs.getString("role")) {
            case "MANAGER"  -> new Manager(id, username, password);
            case "EMPLOYEE" -> new Employee(id, username, password);
            default         -> new Customer(id, username, password);
        };
    }
}
