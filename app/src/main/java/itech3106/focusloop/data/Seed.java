package itech3106.focusloop.data;

import java.util.ArrayList;
import java.util.List;

/**
 * First-run content. The stores write this once so a fresh install still has something
 * to show in a demo, then every later change is persisted by the stores.
 */
public final class Seed {

    private static final String NOTES =
            "Reduce the case study page to one column, keep the process photos, and cut the copy "
                    + "in half. Print it at A3 to check the grid before the review.";

    private Seed() {
    }

    public static List<Task> tasks() {
        int today = Dates.today();
        List<Task> tasks = new ArrayList<>();
        tasks.add(task("t1", "Redesign the case study layout", 'A', "Today", "Q1", NOTES,
                Dates.at(today, 18, 0), true));
        tasks.add(task("t2", "Reply to the client about scope", 'A', "Today", "Q1",
                "Confirm the two extra screens before Friday.", Dates.at(today, 18, 0), false));
        tasks.add(task("t4", "Book the dentist", 'C', "Today", "Q3",
                "The check-up is overdue by two months.", Dates.at(today, 18, 0), false));
        tasks.add(task("t3", "Outline the A2 database schema", 'B', "Tomorrow", "Q2",
                "Three tables is enough for the prototype.", Dates.at(today + 1, 18, 0), false));
        tasks.add(task("t5", "Read chapter 7 on notifications", 'B', "Fri 30 Oct", "Q2",
                "Focus on channels and permission states.", 0L, false));
        tasks.add(task("t6", "Clean up Figma components", 'D', "No date", "Q4",
                "Merge the duplicate button styles.", 0L, false));
        tasks.add(task("t7", "Plan the weekend trip", 'D', "No date", "Q4",
                "Check the train times on Thursday.", 0L, false));
        tasks.add(task("t8", "Update the resume", 'E', "No date", "Q4",
                "Add the focus app once the prototype is done.", 0L, false));
        return tasks;
    }

    private static Task task(String id, String title, char priority, String due, String quadrant,
                             String notes, long dueAt, boolean frog) {
        Task task = new Task(id, title, priority, due, quadrant, notes, false);
        task.dueAt = dueAt;
        task.frog = frog;
        return task;
    }

    public static List<FocusSession> sessions() {
        int today = Dates.today();
        List<FocusSession> sessions = new ArrayList<>();
        sessions.add(new FocusSession("seed-s1", Dates.at(today, 9, 12), "t1",
                "Redesign the case study layout", 25, true, 1));
        sessions.add(new FocusSession("seed-s2", Dates.at(today, 10, 5), "t2",
                "Reply to the client about scope", 25, true, 2));
        sessions.add(new FocusSession("seed-s3", Dates.at(today, 11, 40), "t3",
                "Outline the A2 database schema", 20, false, 3));
        sessions.add(new FocusSession("seed-s4", Dates.at(today - 1, 14, 0), "t5",
                "Read chapter 7 on notifications", 25, true, 1));
        sessions.add(new FocusSession("seed-s5", Dates.at(today - 1, 15, 10), "t6",
                "Clean up Figma components", 25, true, 2));
        sessions.add(new FocusSession("seed-s6", Dates.at(today - 2, 8, 50), "t1",
                "Redesign the case study layout", 50, true, 1));
        return sessions;
    }

    /** A full hour list for one day; only today starts with demo blocks. */
    public static List<TimeSlot> slots(int epochDay) {
        List<TimeSlot> slots = new ArrayList<>();
        boolean today = Dates.isToday(epochDay);
        for (int hour = 8; hour <= 22; hour++) {
            String label = String.format("%02d:00", hour);
            slots.add(new TimeSlot(epochDay, label,
                    today ? blockTitle(hour) : null,
                    today ? blockMeta(hour) : null));
        }
        return slots;
    }

    private static String blockTitle(int hour) {
        switch (hour) {
            case 9:
                return "Team standup";
            case 10:
                return "Redesign case study";
            case 11:
                return "Client email";
            case 13:
                return "A2 schema";
            case 16:
                return "Reading";
            default:
                return null;
        }
    }

    private static String blockMeta(int hour) {
        switch (hour) {
            case 9:
                return "09:00 – 09:30 · 30 min";
            case 10:
                return "10:00 – 11:00 · 60 min";
            case 11:
                return "11:00 – 11:30 · 30 min";
            case 13:
                return "13:00 – 14:30 · 90 min";
            case 16:
                return "16:00 – 16:45 · 45 min";
            default:
                return null;
        }
    }

    public static List<Review> reviews() {
        int today = Dates.today();
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("seed-r1", today - 1,
                "Finished the case study layout before lunch.",
                "Messages broke the second session.",
                "Outline the A2 schema.",
                2, null, Dates.at(today - 1, 21, 30)));
        reviews.add(new Review("seed-r2", today - 3,
                "Two clean focus sessions and a walk.",
                "The plan was too full after 16:00.",
                "Book the dentist.",
                3, null, Dates.at(today - 3, 21, 10)));
        return reviews;
    }
}
