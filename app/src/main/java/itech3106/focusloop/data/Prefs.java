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
