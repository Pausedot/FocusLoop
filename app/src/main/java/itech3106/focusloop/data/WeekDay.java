package itech3106.focusloop.data;

import java.util.List;

/** One day card in the weekly plan view. */
public class WeekDay {

    public final String label;
    public final String date;
    public final int minutes;
    public final boolean today;
    public final boolean allDone;
    public final List<WeekTask> tasks;

    public WeekDay(String label, String date, int minutes, boolean today,
                   boolean allDone, List<WeekTask> tasks) {
        this.label = label;
        this.date = date;
        this.minutes = minutes;
        this.today = today;
        this.allDone = allDone;
        this.tasks = tasks;
    }

    public static class WeekTask {
        public final String id;
        public final String title;

        public WeekTask(String id, String title) {
            this.id = id;
            this.title = title;
        }
    }
}
