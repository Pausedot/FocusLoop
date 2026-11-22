package itech3106.focusloop.focus;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

import itech3106.focusloop.BaseActivity;
import itech3106.focusloop.Extras;
import itech3106.focusloop.MainActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.Prefs;

/**
 * The other half of the loop: a break that starts by itself, can be skipped, and hands
 * the user back to the five-second start for the next session.
 */
public class BreakActivity extends BaseActivity {

    private CountDownTimer timer;
    private long breakMs;
    private long remainingMs;
    private TextView countText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break);
        bindChrome(0);

        breakMs = Prefs.breakMinutes(this) * 60_000L;
        remainingMs = breakMs;

        countText = findViewById(R.id.breakCount);
        String taskTitle = getIntent().getStringExtra(Extras.TASK_TITLE);
        ((TextView) findViewById(R.id.breakTask)).setText(
                taskTitle == null ? getString(R.string.focus_task_default) : taskTitle);
        ((TextView) findViewById(R.id.breakLength)).setText(
                getString(R.string.break_length_format, Prefs.breakMinutes(this)));

        findViewById(R.id.skipText).setOnClickListener(v -> backToTimer());

        updateCount();
        startTimer();
    }

    private void startTimer() {
        timer = new CountDownTimer(remainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                updateCount();
            }

            @Override
            public void onFinish() {
                remainingMs = 0;
                updateCount();
                onBreakFinished();
            }
        }.start();
    }

    private void updateCount() {
        int totalSeconds = (int) Math.ceil(remainingMs / 1000.0);
        countText.setText(String.format(Locale.ENGLISH, "%02d:%02d",
                totalSeconds / 60, totalSeconds % 60));
    }

    private void onBreakFinished() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.break_end_title)
                .setMessage(R.string.break_end_message)
                .setCancelable(false)
                .setPositiveButton(R.string.break_next_focus, (dialog, which) -> openFiveSecond())
                .setNegativeButton(R.string.break_done, (dialog, which) -> goHome())
                .show();
    }

    private void openFiveSecond() {
        Intent intent = new Intent(this, FiveSecondActivity.class);
        intent.putExtra(Extras.TASK_ID, getIntent().getStringExtra(Extras.TASK_ID));
        intent.putExtra(Extras.TASK_TITLE, getIntent().getStringExtra(Extras.TASK_TITLE));
        startActivity(intent);
        finish();
    }

    private void backToTimer() {
        Intent intent = new Intent(this, PomodoroActivity.class);
        intent.putExtra(Extras.TASK_ID, getIntent().getStringExtra(Extras.TASK_ID));
        intent.putExtra(Extras.TASK_TITLE, getIntent().getStringExtra(Extras.TASK_TITLE));
        startActivity(intent);
        finish();
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }
}
