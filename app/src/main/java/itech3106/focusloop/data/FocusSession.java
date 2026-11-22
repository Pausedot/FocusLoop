package itech3106.focusloop.data;

/**
 * One focus session.
 *
 * <p>A1 stored the day and time as display strings ("26 OCT", "09:12"). A2 keeps those
 * fields for the existing row layout but derives them from {@link #startedAt}, so the
 * history can be grouped and totalled by real local day.
 */
public class FocusSession {

    public final String id;
    public final long startedAt;
    public final String taskId;
    /** Display snapshot, for example "22 NOV". */
    public final String group;
    /** Display snapshot, for example "09:12". */
    public final String time;
    public final String title;
    public final int minutes;
    public final boolean completed;
    /** 1-based position in the four-session cycle. */
    public final int round;

    /** Compatibility constructor for A1 demo rows that already carry display strings. */
    public FocusSession(String group, String time, String title, int minutes, boolean completed) {
        this("legacy-" + group + "-" + time + "-" + title, 0L, null, group, time,
                title, minutes, completed, 0);
    }

    /** Constructor for a real session; the display strings are derived from the start time. */
    public FocusSession(String id, long startedAt, String taskId, String title,
                        int minutes, boolean completed, int round) {
        this(id, startedAt, taskId, Dates.dayLabel(startedAt), Dates.timeLabel(startedAt),
                title, minutes, completed, round);
    }

    private FocusSession(String id, long startedAt, String taskId, String group, String time,
                         String title, int minutes, boolean completed, int round) {
        this.id = id;
        this.startedAt = startedAt;
        this.taskId = taskId;
        this.group = group;
        this.time = time;
        this.title = title;
        this.minutes = minutes;
        this.completed = completed;
        this.round = round;
    }
}
