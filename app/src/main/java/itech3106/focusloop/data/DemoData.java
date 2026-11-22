package itech3106.focusloop.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Compatibility facade for the A1 screens during the A2 migration.
 *
 * <p>The real content now lives in {@link Seed}; this class keeps the old static
 * accessors working until every screen reads the stores in Phase 3.
 */
public final class DemoData {

    private static final Set<String> DONE_IDS = new HashSet<>();

    private DemoData() {
    }

    public static void setDone(String id, boolean done) {
        if (done) {
            DONE_IDS.add(id);
        } else {
            DONE_IDS.remove(id);
        }
    }

    public static List<Task> tasks() {
        List<Task> tasks = Seed.tasks();
        for (Task task : tasks) {
            task.done = DONE_IDS.contains(task.id);
        }
        return tasks;
    }

    public static Task task(String id) {
        for (Task task : tasks()) {
            if (task.id.equals(id)) {
                return task;
            }
        }
        return tasks().get(0);
    }

    public static Task taskByTitle(String title) {
        for (Task task : tasks()) {
            if (task.title.equals(title)) {
                return task;
            }
        }
        return tasks().get(0);
    }

    /**
     * Turns a task list into RecyclerView rows: a header string, then its tasks.
     * The header is a String, the task rows are {@link Task}.
     */
    public static List<Object> groupRows(List<Task> tasks) {
        List<Task> today = new ArrayList<>();
        List<Task> later = new ArrayList<>();
        for (Task task : tasks) {
            if ("Today".equals(task.due)) {
                today.add(task);
            } else {
                later.add(task);
            }
        }
        List<Object> rows = new ArrayList<>();
        if (!today.isEmpty()) {
            rows.add("TODAY");
            rows.addAll(today);
        }
        if (!later.isEmpty()) {
            rows.add("LATER");
            rows.addAll(later);
        }
        return rows;
    }

    public static List<FocusSession> sessions() {
        return Seed.sessions();
    }

    /** Same trick as the task list: date header string, then session rows. */
    public static List<Object> sessionRows() {
        List<Object> rows = new ArrayList<>();
        String group = null;
        for (FocusSession session : sessions()) {
            if (!session.group.equals(group)) {
                group = session.group;
                rows.add(group);
            }
            rows.add(session);
        }
        return rows;
    }

    public static List<TimeSlot> slots() {
        return Seed.slots(Dates.today());
    }

    public static List<WeekDay> weekDays() {
        List<WeekDay> days = new ArrayList<>();
        days.add(new WeekDay("MON", "26", 60, false, false, weekTasks("t1")));
        days.add(new WeekDay("TUE", "27", 45, false, false, weekTasks("t3")));
        days.add(new WeekDay("WED", "28", 75, true, false, weekTasks("t1", "t2", "t5")));
        days.add(new WeekDay("THU", "29", 25, false, false, weekTasks("t5")));
        days.add(new WeekDay("FRI", "30", 45, false, true, weekTasks("t6")));
        days.add(new WeekDay("SAT", "31", 30, false, false, weekTasks("t7")));
        days.add(new WeekDay("SUN", "1", 0, false, false, new ArrayList<>()));
        return days;
    }

    private static List<WeekDay.WeekTask> weekTasks(String... ids) {
        List<WeekDay.WeekTask> tasks = new ArrayList<>();
        for (String id : ids) {
            Task task = task(id);
            tasks.add(new WeekDay.WeekTask(task.id, task.title));
        }
        return tasks;
    }

    public static List<ChecklistItem> checklist() {
        List<ChecklistItem> items = new ArrayList<>();
        items.add(new ChecklistItem("Reply to Sam's message", true));
        items.add(new ChecklistItem("Send the invoice", false));
        items.add(new ChecklistItem("Book the dentist", true));
        items.add(new ChecklistItem("Water the plants", false));
        items.add(new ChecklistItem("Print the A3 draft", false));
        return items;
    }
}
