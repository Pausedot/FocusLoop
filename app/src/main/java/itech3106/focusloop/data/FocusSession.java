package itech3106.focusloop.data;

/** One finished or abandoned pomodoro, grouped by day in the history list. */
public class FocusSession {

    public final String group;
    public final String time;
    public final String title;
    public final int minutes;
    public final boolean completed;

    public FocusSession(String group, String time, String title, int minutes, boolean completed) {
        this.group = group;
        this.time = time;
        this.title = title;
        this.minutes = minutes;
        this.completed = completed;
    }
}
