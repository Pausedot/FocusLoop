package itech3106.focusloop.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Static demo content for Assignment 1. Diagrams and lists need something to show,
 * but nothing here is saved, sorted by a database or read from a network.
 */
public final class DemoData {

    private static final String NOTES =
            "Reduce the case study page to one column, keep the process photos, and cut the copy "
                    + "in half. Print it at A3 to check the grid before the review.";

    private DemoData() {
    }

    public static List<Task> tasks() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task("t1", "Redesign the case study layout", 'A', "Today", "Q1", NOTES, false));
        tasks.add(new Task("t2", "Reply to the client about scope", 'A', "Today", "Q1",
                "Confirm the two extra screens before Friday.", false));
        tasks.add(new Task("t4", "Book the dentist", 'C', "Today", "Q3",
                "The check-up is overdue by two months.", false));
        tasks.add(new Task("t3", "Outline the A2 database schema", 'B', "Tomorrow", "Q2",
                "Three tables is enough for the prototype.", false));
        tasks.add(new Task("t5", "Read chapter 7 on notifications", 'B', "Fri 30 Oct", "Q2",
                "Focus on channels and permission states.", false));
        tasks.add(new Task("t6", "Clean up Figma components", 'D', "No date", "Q4",
                "Merge the duplicate button styles.", false));
        tasks.add(new Task("t7", "Plan the weekend trip", 'D', "No date", "Q4",
                "Check the train times on Thursday.", false));
        tasks.add(new Task("t8", "Update the resume", 'E', "No date", "Q4",
                "Add the focus app once the prototype is done.", false));
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
        List<FocusSession> sessions = new ArrayList<>();
        sessions.add(new FocusSession("26 OCT", "09:12", "Redesign the case study layout", 25, true));
        sessions.add(new FocusSession("26 OCT", "10:05", "Reply to the client about scope", 25, true));
        sessions.add(new FocusSession("26 OCT", "11:40", "Outline the A2 database schema", 20, false));
        sessions.add(new FocusSession("25 OCT", "14:00", "Read chapter 7 on notifications", 25, true));
        sessions.add(new FocusSession("25 OCT", "15:10", "Clean up Figma components", 25, true));
        sessions.add(new FocusSession("24 OCT", "08:50", "Redesign the case study layout", 50, true));
        return sessions;
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
        List<TimeSlot> slots = new ArrayList<>();
        slots.add(new TimeSlot("08:00", null, null));
        slots.add(new TimeSlot("09:00", "Team standup", "09:00 – 09:30 · 30 min"));
        slots.add(new TimeSlot("10:00", "Redesign case study", "10:00 – 11:00 · 60 min"));
        slots.add(new TimeSlot("11:00", "Client email", "11:00 – 11:30 · 30 min"));
        slots.add(new TimeSlot("12:00", null, null));
        slots.add(new TimeSlot("13:00", "A2 schema", "13:00 – 14:30 · 90 min"));
        slots.add(new TimeSlot("14:00", null, null));
        slots.add(new TimeSlot("15:00", null, null));
        slots.add(new TimeSlot("16:00", "Reading", "16:00 – 16:45 · 45 min"));
        slots.add(new TimeSlot("17:00", null, null));
        slots.add(new TimeSlot("18:00", null, null));
        slots.add(new TimeSlot("19:00", null, null));
        slots.add(new TimeSlot("20:00", null, null));
        slots.add(new TimeSlot("21:00", null, null));
        slots.add(new TimeSlot("22:00", null, null));
        return slots;
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
