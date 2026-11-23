package itech3106.focusloop.focus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import itech3106.focusloop.BaseActivity;
import itech3106.focusloop.Extras;
import itech3106.focusloop.R;

public class FiveSecondActivity extends BaseActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView countText;
    private View[] dots;
    private int count = 5;
    private boolean launched;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            count--;
            if (count > 0) {
                countText.setText(String.valueOf(count));
                updateDots(6 - count);
                handler.postDelayed(this, 1000);
            } else {
                updateDots(5);
                countText.setText(R.string.five_go);
                countText.setTextSize(140f);
                countText.setTextColor(ContextCompat.getColor(FiveSecondActivity.this,
                        R.color.accent_on_dark));
                ((TextView) findViewById(R.id.fiveHint)).setText(R.string.five_starting);
                // GO starts the session; the short pause lets the state be seen.
                handler.postDelayed(() -> openPomodoro(true), 600);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_five_second);
        bindChrome(0);

        countText = findViewById(R.id.countText);
        dots = new View[]{findViewById(R.id.dot1), findViewById(R.id.dot2),
                findViewById(R.id.dot3), findViewById(R.id.dot4), findViewById(R.id.dot5)};
        String taskTitle = getIntent().getStringExtra(Extras.TASK_TITLE);
        TextView taskName = findViewById(R.id.fiveTaskName);
        taskName.setText(taskTitle == null ? getString(R.string.focus_task_default) : taskTitle);

        handler.postDelayed(tick, 1000);

        GestureDetector gestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                        if (e1 != null && e2 != null
                                && e2.getY() - e1.getY() > 120
                                && Math.abs(vy) > Math.abs(vx)) {
                            openPomodoro(false);
                            return true;
                        }
                        return false;
                    }
                });
        findViewById(R.id.root).setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });

        findViewById(R.id.root).setOnClickListener(v -> openPomodoro(true));
        findViewById(R.id.skipText).setOnClickListener(v -> openPomodoro(false));
    }

    private void updateDots(int filled) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(i < filled
                    ? R.drawable.bg_dot_fixed_accent
                    : R.drawable.bg_dot_dim);
        }
    }

    private void openPomodoro(boolean autoStart) {
        if (launched) {
            return;
        }
        launched = true;
        Intent intent = new Intent(this, PomodoroActivity.class);
        intent.putExtra(Extras.TASK_ID, getIntent().getStringExtra(Extras.TASK_ID));
        intent.putExtra(Extras.TASK_TITLE, getIntent().getStringExtra(Extras.TASK_TITLE));
        intent.putExtra(Extras.AUTO_START, autoStart);
        startActivity(intent);
        finish();
    }

    @Override
    protected boolean drawsOnDark() {
        return true;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(tick);
        super.onDestroy();
    }
}
