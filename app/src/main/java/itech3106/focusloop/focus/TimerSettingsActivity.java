package itech3106.focusloop.focus;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;

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

        findViewById(R.id.btnRestore).setOnClickListener(v -> restoreDefaults());
        findViewById(R.id.btnSaveSettings).setOnClickListener(v -> finish());
    }

    private void restoreDefaults() {
        focusSlider.setValue(25f);
        breakSlider.setValue(5f);
        soundSwitch.setChecked(true);
        darkSwitch.setChecked(false);
        focusValue.setText(R.string.settings_focus_value);
        breakValue.setText(R.string.settings_break_value);
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
