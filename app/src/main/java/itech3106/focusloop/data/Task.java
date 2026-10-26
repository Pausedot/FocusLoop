package itech3106.focusloop.data;

/** A task as shown in the prototype. A1 only displays these, nothing is persisted. */
public class Task {

    public final String id;
    public final String title;
    public final String notes;
    public final char priority;
    public final String due;
    public final String quadrant;
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
