package cinema.model;


public class Hall {

    private final String id;
    private final String name;
    private final int capacity;

    public Hall(String id, String name, int capacity) {
        this.id       = id;
        this.name     = name;
        this.capacity = capacity;
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public int getCapacity()    { return capacity; }

    @Override
    public String toString() {
        return "Hall{id='" + id + "', name='" + name
                + "', capacity=" + capacity + "}";
    }
}
