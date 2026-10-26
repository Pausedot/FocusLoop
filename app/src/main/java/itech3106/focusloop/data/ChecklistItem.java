package itech3106.focusloop.data;

/** One line of the two minute list. */
public class ChecklistItem {

    public String text;
    public boolean done;

    public ChecklistItem(String text, boolean done) {
        this.text = text;
        this.done = done;
    }
}
