package itech3106.focusloop.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Small A1 compatibility helpers that are still used by the current screens.
 * Everything else now reads the real stores in {@link Seed} and the store classes.
 */
public final class DemoData {

    private DemoData() {
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

    /** First-run items for the two-minute list. Later edits live in the activity. */
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
