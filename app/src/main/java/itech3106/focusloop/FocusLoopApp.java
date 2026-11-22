package itech3106.focusloop;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import itech3106.focusloop.data.Prefs;

/** Applies the saved theme before the first screen is created. */
public class FocusLoopApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(Prefs.darkMode(this)
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
