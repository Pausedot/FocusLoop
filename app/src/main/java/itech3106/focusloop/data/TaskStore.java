package itech3106.focusloop.data;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Tasks and their completion state. */
public class TaskStore extends JsonStore<Task> {

    public TaskStore(Context context) {
        super(context, "tasks_v1");
        seedOnce(Seed.tasks());
    }

    @Override
    protected String idOf(Task item) {
        return item.id;
    }

    @Override
    protected JSONObject toJson(Task task) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", task.id);
        json.put("title", task.title);
        json.put("notes", task.notes == null ? "" : task.notes);
        json.put("priority", String.valueOf(task.priority));
        json.put("due", task.due);
        json.put("dueAt", task.dueAt);
        json.put("quadrant", task.quadrant);
        json.put("done", task.done);
        json.put("completedAt", task.completedAt);
        json.put("frog", task.frog);
        json.put("attachment", task.attachmentPath == null ? "" : task.attachmentPath);
        return json;
    }

    @Override
    protected Task fromJson(JSONObject json) throws JSONException {
        String priority = json.optString("priority", "C");
        Task task = new Task(
                json.getString("id"),
                json.optString("title", ""),
                priority.isEmpty() ? 'C' : priority.charAt(0),
                json.optString("due", "No date"),
                json.optString("quadrant", "Q4"),
                json.optString("notes", ""),
                json.optBoolean("done", false));
        task.dueAt = json.optLong("dueAt", 0);
        task.completedAt = json.optLong("completedAt", 0);
        task.frog = json.optBoolean("frog", false);
        String attachment = json.optString("attachment", "");
        task.attachmentPath = attachment.isEmpty() ? null : attachment;
        return task;
    }

    public List<Task> open() {
        List<Task> open = new ArrayList<>();
        for (Task task : all()) {
            if (!task.done) {
                open.add(task);
            }
        }
        Collections.sort(open, Comparator
                .comparingInt((Task task) -> task.priority)
                .thenComparing(task -> task.title));
        return open;
    }

    public List<Task> done() {
        List<Task> done = new ArrayList<>();
        for (Task task : all()) {
            if (task.done) {
                done.add(task);
            }
        }
        Collections.sort(done, (left, right) -> Long.compare(right.completedAt, left.completedAt));
        return done;
    }

    /** The explicit frog, otherwise the highest-priority open task. */
    public Task frog() {
        for (Task task : all()) {
            if (task.frog && !task.done) {
                return task;
            }
        }
        List<Task> open = open();
        return open.isEmpty() ? null : open.get(0);
    }

    public void setDone(String id, boolean done) {
        Task task = byId(id);
        if (task == null) {
            return;
        }
        task.done = done;
        task.completedAt = done ? System.currentTimeMillis() : 0L;
        upsert(task);
    }

    public void setFrog(String id) {
        List<Task> tasks = all();
        for (Task task : tasks) {
            task.frog = task.id.equals(id);
        }
        replaceAll(tasks);
    }

    public List<Task> dueOn(int epochDay) {
        List<Task> due = new ArrayList<>();
        for (Task task : all()) {
            if (!task.done && task.dueAt > 0 && Dates.epochDay(task.dueAt) == epochDay) {
                due.add(task);
            }
        }
        return due;
    }

    public List<Task> completedOn(int epochDay) {
        List<Task> completed = new ArrayList<>();
        for (Task task : all()) {
            if (task.done && task.completedAt > 0
                    && Dates.epochDay(task.completedAt) == epochDay) {
                completed.add(task);
            }
        }
        return completed;
    }
}
