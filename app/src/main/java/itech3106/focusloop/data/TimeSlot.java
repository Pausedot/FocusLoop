package itech3106.focusloop.data;

/** One hour of the day plan. Title and meta are mutable so the edit dialog can update the row. */
public class TimeSlot {

    public final String hour;
    public String title;
    public String meta;

    public TimeSlot(String hour, String title, String meta) {
        this.hour = hour;
        this.title = title;
        this.meta = meta;
    }
}
