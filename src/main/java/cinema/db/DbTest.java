package cinema.db;

import cinema.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Szybki test połączenia z bazą – uruchom żeby sprawdzić czy działa.
 *
 * Przed uruchomieniem:
 *   1. Upewnij się że MySQL działa i baza cinema_db istnieje
 *      (uruchom skrypt cinema_km1.sql)
 *   2. Ustaw login/hasło w DatabaseConnection.java (domyślnie root/root)
 *   3. Uruchom: mvn compile exec:java -Dexec.mainClass="cinema.db.DbTest"
 */
public class DbTest {

    public static void main(String[] args) {
        System.out.println("=== Test połączenia z bazą MySQL ===\n");

        try {
            UserRepository repo = new UserRepository();

            // 1. Pobierz wszystkich użytkowników
            System.out.println("--- Wszyscy użytkownicy ---");
            List<User> users = repo.findAll();
            users.forEach(u -> System.out.println("  " + u));

            // 2. Poprawne logowanie
            System.out.println("\n--- Logowanie: jan.kowalski / haslo1 ---");
            Optional<User> user = repo.authenticate("jan.kowalski", "haslo1");
            user.ifPresentOrElse(
                u  -> System.out.println("  OK: " + u),
                () -> System.out.println("  FAIL: nie znaleziono")
            );

            // 3. Błędne hasło
            System.out.println("\n--- Logowanie: jan.kowalski / zle_haslo ---");
            Optional<User> user2 = repo.authenticate("jan.kowalski", "zle_haslo");
            user2.ifPresentOrElse(
                u  -> System.out.println("  OK: " + u),
                () -> System.out.println("  FAIL: błędne dane (oczekiwane)")
            );

            // 4. Logowanie menadżera
            System.out.println("\n--- Logowanie: admin / admin123 ---");
            Optional<User> mgr = repo.authenticate("admin", "admin123");
            mgr.ifPresentOrElse(
                u  -> System.out.println("  OK: " + u.getClass().getSimpleName() + " – " + u.getUsername()),
                () -> System.out.println("  FAIL: nie znaleziono")
            );

            DatabaseConnection.getInstance().close();
            System.out.println("\n=== Test zakończony pomyślnie ===");

        } catch (RuntimeException e) {
            System.err.println("\n[BŁĄD] Nie można połączyć z bazą danych!");
            System.err.println("Sprawdź:");
            System.err.println("  1. Czy MySQL działa (port 3306)");
            System.err.println("  2. Czy baza cinema_db istnieje (uruchom cinema_km1.sql)");
            System.err.println("  3. Czy login/hasło w DatabaseConnection.java są poprawne");
            System.err.println("\nSzczegóły: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("\n[BŁĄD SQL] " + e.getMessage());
        }
    }
}
