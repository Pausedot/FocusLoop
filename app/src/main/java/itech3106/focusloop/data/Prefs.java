package itech3106.focusloop.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Timer and appearance settings. A1 built the controls but saved nothing; A2 reads and
 * writes them here so the timer, the break screen, the chime and the theme agree.
 */
public final class Prefs {

    public static final int DEFAULT_FOCUS = 25;
    public static final int DEFAULT_BREAK = 5;

    private static final String FILE = "focusloop_settings";
    private static final String KEY_FOCUS = "focus_minutes";
    private static final String KEY_BREAK = "break_minutes";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_DARK = "dark_mode";
    private static final String KEY_TIMER_RUNNING = "timer_running";
    private static final String KEY_TIMER_END = "timer_end";
    private static final String KEY_TIMER_REMAINING = "timer_remaining";
    private static final String KEY_TIMER_TASK_ID = "timer_task_id";
    private static final String KEY_TIMER_TASK_TITLE = "timer_task_title";

    private Prefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static int focusMinutes(Context context) {
        return clamp(prefs(context).getInt(KEY_FOCUS, DEFAULT_FOCUS), 5, 60);
    }

    public static void setFocusMinutes(Context context, int minutes) {
        prefs(context).edit().putInt(KEY_FOCUS, clamp(minutes, 5, 60)).apply();
    }

    public static int breakMinutes(Context context) {
        return clamp(prefs(context).getInt(KEY_BREAK, DEFAULT_BREAK), 1, 15);
    }

    public static void setBreakMinutes(Context context, int minutes) {
        prefs(context).edit().putInt(KEY_BREAK, clamp(minutes, 1, 15)).apply();
    }

    public static boolean soundEnabled(Context context) {
        return prefs(context).getBoolean(KEY_SOUND, true);
    }

    public static void setSoundEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_SOUND, enabled).apply();
    }

    public static boolean darkMode(Context context) {
        return prefs(context).getBoolean(KEY_DARK, false);
    }

    public static void setDarkMode(Context context, boolean dark) {
        prefs(context).edit().putBoolean(KEY_DARK, dark).apply();
    }

    public static void saveTimerRunning(Context context, long endAt,
                                        String taskId, String taskTitle) {
        prefs(context).edit()
                .putBoolean(KEY_TIMER_RUNNING, true)
                .putLong(KEY_TIMER_END, endAt)
                .putString(KEY_TIMER_TASK_ID, taskId == null ? "" : taskId)
                .putString(KEY_TIMER_TASK_TITLE, taskTitle == null ? "" : taskTitle)
                .apply();
    }

    public static void saveTimerPaused(Context context, long remaining,
                                       String taskId, String taskTitle) {
        prefs(context).edit()
                .putBoolean(KEY_TIMER_RUNNING, false)
                .putLong(KEY_TIMER_REMAINING, remaining)
                .putString(KEY_TIMER_TASK_ID, taskId == null ? "" : taskId)
                .putString(KEY_TIMER_TASK_TITLE, taskTitle == null ? "" : taskTitle)
                .apply();
    }

    public static boolean timerRunning(Context context) {
        return prefs(context).getBoolean(KEY_TIMER_RUNNING, false);
    }

    public static long timerEndAt(Context context) {
        return prefs(context).getLong(KEY_TIMER_END, 0L);
    }

    public static long timerRemaining(Context context) {
        return prefs(context).getLong(KEY_TIMER_REMAINING, 0L);
    }

    public static String timerTaskId(Context context) {
        String value = prefs(context).getString(KEY_TIMER_TASK_ID, "");
        return value == null || value.isEmpty() ? null : value;
    }

    public static String timerTaskTitle(Context context) {
        String value = prefs(context).getString(KEY_TIMER_TASK_TITLE, "");
        return value == null || value.isEmpty() ? null : value;
    }

    public static void clearTimer(Context context) {
        prefs(context).edit()
                .remove(KEY_TIMER_RUNNING)
                .remove(KEY_TIMER_END)
                .remove(KEY_TIMER_REMAINING)
                .remove(KEY_TIMER_TASK_ID)
                .remove(KEY_TIMER_TASK_TITLE)
                .apply();
    }

    public static void restoreDefaults(Context context) {
        prefs(context).edit()
                .putInt(KEY_FOCUS, DEFAULT_FOCUS)
                .putInt(KEY_BREAK, DEFAULT_BREAK)
                .putBoolean(KEY_SOUND, true)
                .putBoolean(KEY_DARK, false)
                .apply();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
