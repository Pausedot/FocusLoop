package itech3106.focusloop.data;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Finished and stopped focus sessions. */
public class SessionStore extends JsonStore<FocusSession> {

    public SessionStore(Context context) {
        super(context, "sessions_v1");
        seedOnce(Seed.sessions());
    }

    @Override
    protected String idOf(FocusSession item) {
        return item.id;
    }

    @Override
    protected JSONObject toJson(FocusSession session) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", session.id);
        json.put("startedAt", session.startedAt);
        json.put("taskId", session.taskId == null ? "" : session.taskId);
        json.put("title", session.title);
        json.put("minutes", session.minutes);
        json.put("completed", session.completed);
        json.put("round", session.round);
        return json;
    }

    @Override
    protected FocusSession fromJson(JSONObject json) throws JSONException {
        String taskId = json.optString("taskId", "");
        return new FocusSession(
                json.getString("id"),
                json.optLong("startedAt", 0L),
                taskId.isEmpty() ? null : taskId,
                json.optString("title", ""),
                json.optInt("minutes", 0),
                json.optBoolean("completed", false),
                json.optInt("round", 0));
    }

    public List<FocusSession> recent() {
        List<FocusSession> sessions = all();
        Collections.sort(sessions, (left, right) -> Long.compare(right.startedAt, left.startedAt));
        return sessions;
    }

    public List<FocusSession> today() {
        int today = Dates.today();
        List<FocusSession> sessions = new ArrayList<>();
        for (FocusSession session : recent()) {
            if (Dates.epochDay(session.startedAt) == today) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    public int minutesToday() {
        int minutes = 0;
        for (FocusSession session : today()) {
            minutes += session.minutes;
        }
        return minutes;
    }

    public int completedToday() {
        int count = 0;
        for (FocusSession session : today()) {
            if (session.completed) {
                count++;
            }
        }
        return count;
    }

    public int minutesForTask(String taskId) {
        if (taskId == null) {
            return 0;
        }
        int minutes = 0;
        for (FocusSession session : all()) {
            if (taskId.equals(session.taskId)) {
                minutes += session.minutes;
            }
        }
        return minutes;
    }

    public int countForTask(String taskId) {
        if (taskId == null) {
            return 0;
        }
        int count = 0;
        for (FocusSession session : all()) {
            if (taskId.equals(session.taskId)) {
                count++;
            }
        }
        return count;
    }

    public void record(FocusSession session) {
        add(session);
    }
}
