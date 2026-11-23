package itech3106.focusloop.data;

/**
 * One hour of the day plan. A2 adds the local epoch day so the plan is not limited to a
 * single hardcoded day.
 */
public class TimeSlot {

    public final int epochDay;
    public final String hour;
    public String title;
    public String meta;
    public String taskId;

    public TimeSlot(String hour, String title, String meta) {
        this(Dates.today(), hour, title, meta);
    }

    public TimeSlot(int epochDay, String hour, String title, String meta) {
        this.epochDay = epochDay;
        this.hour = hour;
        this.title = title;
        this.meta = meta;
    }

    public String id() {
        return epochDay + "|" + hour;
    }
}
