package itech3106.focusloop.data;

/**
 * One task. A1 only displayed these; A2 persists them and adds the fields the new
 * screens need (machine-readable due date, completion time, frog flag, photo path).
 */
public class Task {

    public final String id;
    public final String title;
    public final String notes;
    public final char priority;
    /** Display label kept from A1: "Today", "Tomorrow", "Fri 30 Oct", "No date". */
    public final String due;
    public final String quadrant;
    /** Machine-readable due date in epoch milliseconds; 0 when there is no date. */
    public long dueAt;
    /** When the task was completed, epoch milliseconds; 0 while it is open. */
    public long completedAt;
    /** The one task highlighted on Main. */
    public boolean frog;
    /** App-private file path for a photo attachment, or null. */
    public String attachmentPath;
    public boolean done;

    public Task(String id, String title, char priority, String due, String quadrant,
                String notes, boolean done) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.due = due;
        this.quadrant = quadrant;
        this.notes = notes;
        this.done = done;
    }
}
