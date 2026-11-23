package itech3106.focusloop.data;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The hour-by-hour day plan, one collection for every local day. */
public class PlanStore extends JsonStore<TimeSlot> {

    public PlanStore(Context context) {
        super(context, "plan_v1");
    }

    @Override
    protected String idOf(TimeSlot item) {
        return item.id();
    }

    @Override
    protected JSONObject toJson(TimeSlot slot) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("epochDay", slot.epochDay);
        json.put("hour", slot.hour);
        json.put("title", slot.title == null ? "" : slot.title);
        json.put("meta", slot.meta == null ? "" : slot.meta);
        json.put("taskId", slot.taskId == null ? "" : slot.taskId);
        return json;
    }

    @Override
    protected TimeSlot fromJson(JSONObject json) throws JSONException {
        String title = json.optString("title", "");
        String meta = json.optString("meta", "");
        TimeSlot slot = new TimeSlot(
                json.optInt("epochDay", Dates.today()),
                json.getString("hour"),
                title.isEmpty() ? null : title,
                meta.isEmpty() ? null : meta);
        String taskId = json.optString("taskId", "");
        slot.taskId = taskId.isEmpty() ? null : taskId;
        return slot;
    }

    /** Returns the full hour list for a day, creating an empty one on first visit. */
    public synchronized List<TimeSlot> forDay(int epochDay) {
        List<TimeSlot> all = all();
        List<TimeSlot> day = new ArrayList<>();
        for (TimeSlot slot : all) {
            if (slot.epochDay == epochDay) {
                day.add(slot);
            }
        }
        if (day.isEmpty()) {
            day = Seed.slots(epochDay);
            all.addAll(day);
            replaceAll(all);
        }
        Collections.sort(day, (left, right) -> left.hour.compareTo(right.hour));
        return day;
    }

    public void save(TimeSlot slot) {
        upsert(slot);
    }

    public void clear(TimeSlot slot) {
        slot.title = null;
        slot.meta = null;
        slot.taskId = null;
        upsert(slot);
    }

    /** Removes a deleted task from any block that linked to it, keeping the block itself. */
    public synchronized void detachTask(String taskId) {
        if (taskId == null) {
            return;
        }
        List<TimeSlot> all = all();
        boolean changed = false;
        for (TimeSlot slot : all) {
            if (taskId.equals(slot.taskId)) {
                slot.taskId = null;
                changed = true;
            }
        }
        if (changed) {
            replaceAll(all);
        }
    }

    public int plannedMinutes(int epochDay) {
        int minutes = 0;
        for (TimeSlot slot : forDay(epochDay)) {
            if (slot.meta == null) {
                continue;
            }
            int marker = slot.meta.lastIndexOf('·');
            String tail = marker >= 0 ? slot.meta.substring(marker + 1).trim() : slot.meta;
            String digits = tail.replaceAll("[^0-9]", " ");
            String[] parts = digits.trim().split("\\s+");
            try {
                minutes += Integer.parseInt(parts[0]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                // A hand-written meta line without a duration simply does not count.
            }
        }
        return minutes;
    }
}
