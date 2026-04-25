package cinema.service;

import cinema.model.Hall;
import cinema.model.Screening;
import cinema.model.User;

import java.time.LocalDateTime;
import java.util.*;


public class CinemaSystem {

    private static CinemaSystem instance;

    private final List<Screening> screenings = new ArrayList<>();
    private final List<User> users           = new ArrayList<>();

    private CinemaSystem() {}

    public static CinemaSystem getInstance() {
        if (instance == null) {
            instance = new CinemaSystem();
        }
        return instance;
    }


    public static void resetInstance() {
        instance = null;
    }


    public void addScreening(Screening screening) {
        if (!isHallAvailable(screening.getHall(), screening.getStart(), screening.getEnd())) {
            throw new IllegalStateException(
                    "Sala '" + screening.getHall().getName()
                            + "' jest już zajęta w podanym terminie.");
        }
        screenings.add(screening);
    }


    public boolean isHallAvailable(Hall hall, LocalDateTime start, LocalDateTime end) {
        return screenings.stream()
                .filter(s -> s.getHall().getId().equals(hall.getId()))
                .noneMatch(s -> !(s.getEnd().compareTo(start) <= 0
                        || s.getStart().compareTo(end) >= 0));
    }

    public Optional<Screening> findScreening(String id) {
        return screenings.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    public List<Screening> getScreenings() {
        return Collections.unmodifiableList(screenings);
    }

    // ── Użytkownicy ───────────────────────────────────────────────────────

    public void addUser(User user) {
        users.add(user);
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public Optional<User> findUser(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }


    public Optional<User> authenticate(String username, String password) {
        return users.stream()
                .filter(u -> u.authenticate(username, password))
                .findFirst();
    }
}
