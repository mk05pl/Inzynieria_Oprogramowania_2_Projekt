package cinema.model;


public class Seat {

    private final int row;
    private final int number;
    private boolean reserved;
    private final boolean premium;

    public Seat(int row, int number) {
        this(row, number, false);
    }

    public Seat(int row, int number, boolean premium) {
        this.row      = row;
        this.number   = number;
        this.premium  = premium;
        this.reserved = false;
    }

    /** Rezerwuje miejsce. Rzuca wyjątek jeśli już zajęte. */
    public void reserve() {
        if (reserved) throw new IllegalStateException("Miejsce jest już zajęte.");
        this.reserved = true;
    }

    /** Zwalnia miejsce. */
    public void release() {
        this.reserved = false;
    }

    public int getRow()         { return row; }
    public int getNumber()      { return number; }
    public boolean isReserved() { return reserved; }
    public boolean isPremium()  { return premium; }

    @Override
    public String toString() {
        return "Seat{row=" + row + ", number=" + number
                + ", reserved=" + reserved + ", premium=" + premium + "}";
    }
}
