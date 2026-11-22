package itech3106.focusloop.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A tiny list repository on top of SharedPreferences and org.json.
 *
 * <p>The course does not cover databases, and the lists in this app are small (dozens of
 * rows), so a versioned JSON array per collection is enough. Every write rewrites the
 * whole collection; that is fine at this size and keeps the code easy to explain.
 * Collections are separated by key inside one preferences file.
 */
public abstract class JsonStore<T> {

    private static final String FILE = "focusloop_data";

    private final SharedPreferences prefs;
    private final String key;

    protected JsonStore(Context context, String key) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
        this.key = key;
    }

    protected abstract String idOf(T item);

    protected abstract JSONObject toJson(T item) throws JSONException;

    protected abstract T fromJson(JSONObject json) throws JSONException;

    public synchronized List<T> all() {
        List<T> items = new ArrayList<>();
        String raw = prefs.getString(key, null);
        if (raw == null) {
            return items;
        }
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                try {
                    items.add(fromJson(array.getJSONObject(i)));
                } catch (JSONException ignored) {
                    // Skip one corrupt row instead of losing the whole collection.
                }
            }
        } catch (JSONException ignored) {
            // A corrupt collection is treated as empty; the next write repairs it.
        }
        return items;
    }

    public synchronized void replaceAll(List<T> items) {
        JSONArray array = new JSONArray();
        for (T item : items) {
            try {
                array.put(toJson(item));
            } catch (JSONException ignored) {
                // Skip a row that cannot be serialised.
            }
        }
        prefs.edit().putString(key, array.toString()).apply();
    }

    public synchronized void add(T item) {
        List<T> items = all();
        items.add(item);
        replaceAll(items);
    }

    /** Replaces the row with the same id, or adds it when it is new. */
    public synchronized void upsert(T item) {
        List<T> items = all();
        String id = idOf(item);
        for (int i = 0; i < items.size(); i++) {
            if (id.equals(idOf(items.get(i)))) {
                items.set(i, item);
                replaceAll(items);
                return;
            }
        }
        items.add(item);
        replaceAll(items);
    }

    public synchronized void remove(String id) {
        List<T> items = all();
        for (int i = 0; i < items.size(); i++) {
            if (id.equals(idOf(items.get(i)))) {
                items.remove(i);
                replaceAll(items);
                return;
            }
        }
    }

    public synchronized T byId(String id) {
        for (T item : all()) {
            if (idOf(item).equals(id)) {
                return item;
            }
        }
        return null;
    }

    /** Writes the seed list only when this collection has never been written. */
    protected synchronized void seedOnce(List<T> seed) {
        if (!prefs.contains(key)) {
            replaceAll(seed);
        }
    }
}
