package cinema.model;

import java.util.UUID;


public class Film {

    private final String id;
    private final String title;
    private final int durationMinutes;

    public Film(String title, int durationMinutes) {
        this.id              = UUID.randomUUID().toString();
        this.title           = title;
        this.durationMinutes = durationMinutes;
    }

    public Film(String id, String title, int durationMinutes) {
        this.id              = id;
        this.title           = title;
        this.durationMinutes = durationMinutes;
    }

    public String getId()             { return id; }
    public String getTitle()          { return title; }
    public int getDurationMinutes()   { return durationMinutes; }

    @Override
    public String toString() {
        return "Film{id='" + id + "', title='" + title
                + "', duration=" + durationMinutes + "min}";
    }
}
