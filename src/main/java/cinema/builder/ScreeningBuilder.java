package cinema.builder;

import cinema.model.*;
import cinema.service.CinemaSystem;

import java.time.LocalDateTime;

/**
 * Wzorzec Builder – buduje obiekt Screening krok po kroku.
 * Waliduje wszystkie wymagane pola i sprawdza konflikt sal przed build().
 * Autor: Michał Kowalski
 */
public class ScreeningBuilder {

    private Film          film;
    private Hall          hall;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean       is3D = false;

    public ScreeningBuilder setFilm(Film film) {
        this.film = film;
        return this;
    }

    public ScreeningBuilder setHall(Hall hall) {
        this.hall = hall;
        return this;
    }

    public ScreeningBuilder setStart(LocalDateTime start) {
        this.start = start;
        return this;
    }

    public ScreeningBuilder setEnd(LocalDateTime end) {
        this.end = end;
        return this;
    }

    public ScreeningBuilder set3D(boolean is3D) {
        this.is3D = is3D;
        return this;
    }

    /**
     * Automatycznie ustawia czas zakończenia na podstawie czasu trwania filmu.
     */
    public ScreeningBuilder setEndFromFilm() {
        if (film == null)  throw new IllegalStateException("Film musi być ustawiony przed wywołaniem setEndFromFilm().");
        if (start == null) throw new IllegalStateException("Start musi być ustawiony przed wywołaniem setEndFromFilm().");
        this.end = start.plusMinutes(film.getDurationMinutes());
        return this;
    }

    /**
     * Buduje obiekt Screening po walidacji wszystkich pól.
     * Sprawdza dostępność sali w CinemaSystem.
     *
     * @throws IllegalArgumentException gdy brakuje wymaganego pola
     * @throws IllegalStateException    gdy sala jest zajęta w podanym terminie
     */
    public Screening build() {
        validate();

        CinemaSystem system = CinemaSystem.getInstance();
        if (!system.isHallAvailable(hall, start, end)) {
            throw new IllegalStateException(
                "Sala '" + hall.getName() + "' jest już zajęta w terminie "
                + start + " – " + end + ".");
        }

        Screening screening = new Screening(film, hall, start, end, is3D);
        system.addScreening(screening);
        return screening;
    }

    private void validate() {
        if (film  == null) throw new IllegalArgumentException("Film nie został ustawiony.");
        if (hall  == null) throw new IllegalArgumentException("Sala nie została ustawiona.");
        if (start == null) throw new IllegalArgumentException("Czas rozpoczęcia nie został ustawiony.");
        if (end   == null) throw new IllegalArgumentException("Czas zakończenia nie został ustawiony.");
        if (!end.isAfter(start))
            throw new IllegalArgumentException("Czas zakończenia musi być po czasie rozpoczęcia.");
    }

    // Gettery – używane w testach
    public Film          getFilm()  { return film; }
    public Hall          getHall()  { return hall; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd()   { return end; }
    public boolean       is3D()     { return is3D; }
}
