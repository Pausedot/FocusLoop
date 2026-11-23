package itech3106.focusloop.focus;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.Prefs;

public class TimerSettingsActivity extends ModuleActivity {

    private Slider focusSlider;
    private Slider breakSlider;
    private TextView focusValue;
    private TextView breakValue;
    private MaterialSwitch soundSwitch;
    private MaterialSwitch darkSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_settings);
        bindChrome(R.string.title_settings);

        focusSlider = findViewById(R.id.focusSlider);
        breakSlider = findViewById(R.id.breakSlider);
        focusValue = findViewById(R.id.focusLengthValue);
        breakValue = findViewById(R.id.breakLengthValue);
        soundSwitch = findViewById(R.id.soundSwitch);
        darkSwitch = findViewById(R.id.darkSwitch);

        focusSlider.addOnChangeListener((slider, value, fromUser) ->
                focusValue.setText(getString(R.string.settings_value_format, (int) value)));
        breakSlider.addOnChangeListener((slider, value, fromUser) ->
                breakValue.setText(getString(R.string.settings_value_format, (int) value)));

        load();

        findViewById(R.id.btnRestore).setOnClickListener(v -> restoreDefaults());
        findViewById(R.id.btnSaveSettings).setOnClickListener(v -> save());
        findViewById(R.id.soundCard).setOnClickListener(v -> {
            Chime.play(this);
            Toast.makeText(this, R.string.settings_preview_playing, Toast.LENGTH_SHORT).show();
        });
    }

    private void load() {
        int focus = Prefs.focusMinutes(this);
        int pause = Prefs.breakMinutes(this);
        focusSlider.setValue(focus);
        breakSlider.setValue(pause);
        focusValue.setText(getString(R.string.settings_value_format, focus));
        breakValue.setText(getString(R.string.settings_value_format, pause));
        soundSwitch.setChecked(Prefs.soundEnabled(this));
        darkSwitch.setChecked(Prefs.darkMode(this));
    }

    private void restoreDefaults() {
        focusSlider.setValue(Prefs.DEFAULT_FOCUS);
        breakSlider.setValue(Prefs.DEFAULT_BREAK);
        soundSwitch.setChecked(true);
        darkSwitch.setChecked(false);
        focusValue.setText(getString(R.string.settings_value_format, Prefs.DEFAULT_FOCUS));
        breakValue.setText(getString(R.string.settings_value_format, Prefs.DEFAULT_BREAK));
    }

    private void save() {
        Prefs.setFocusMinutes(this, Math.round(focusSlider.getValue()));
        Prefs.setBreakMinutes(this, Math.round(breakSlider.getValue()));
        Prefs.setSoundEnabled(this, soundSwitch.isChecked());

        boolean dark = darkSwitch.isChecked();
        boolean changed = dark != Prefs.darkMode(this);
        Prefs.setDarkMode(this, dark);
        if (changed) {
            AppCompatDelegate.setDefaultNightMode(dark
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        }
        finish();
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_focus;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_timer_settings;
    }
}
