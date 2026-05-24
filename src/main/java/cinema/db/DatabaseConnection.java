package cinema.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/cinema_db"
                                         + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
    private static final String USER     = "root";
    private static final String PASSWORD = "";
    private static DatabaseConnection instance;
    private Connection connection;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Brak sterownika MySQL: " + e.getMessage());
        }
    }

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Połączono z bazą cinema_db.");
        } catch (SQLException e) {
            throw new RuntimeException("Nie można połączyć z bazą: " + e.getMessage(), e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Błąd reconnect: " + e.getMessage(), e);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Rozłączono.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Błąd zamykania: " + e.getMessage());
        }
    }
}
