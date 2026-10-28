package itech3106.focusloop.focus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Locale;

import itech3106.focusloop.Extras;
import itech3106.focusloop.MainActivity;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.DemoData;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.tasks.TaskDetailActivity;
import itech3106.focusloop.tasks.TaskInboxActivity;

public class PomodoroActivity extends ModuleActivity {

    private static final long SESSION_MS = 25 * 60 * 1000L;
    private static final String CHANNEL_ID = "focus_sessions";
    private static final int NOTIFICATION_ID = 1001;
    private static final int REQUEST_NOTIFICATIONS = 2001;

    private TextView timerText;
    private CircularProgressIndicator ringProgress;
    private FloatingActionButton btnPlay;
    private MaterialButton btnPause;
    private MaterialButton btnFiveSecond;

    private CountDownTimer timer;
    private long remainingMs = SESSION_MS;
    private boolean running;
    private String taskTitle;
    private String taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);
        bindChrome(R.string.title_focus);

        taskTitle = getIntent().getStringExtra(Extras.TASK_TITLE);
        taskId = getIntent().getStringExtra(Extras.TASK_ID);
        TextView title = findViewById(R.id.focusTaskTitle);
        TextView meta = findViewById(R.id.focusTaskMeta);
        if (taskTitle == null) {
            title.setText(R.string.focus_task_default);
            meta.setText(R.string.focus_no_task_meta);
        } else {
            title.setText(taskTitle);
            Task task = DemoData.task(taskId == null ? "t1" : taskId);
            meta.setText(task.quadrant + " · " + task.due);
        }

        timerText = findViewById(R.id.timerText);
        ringProgress = findViewById(R.id.ringProgress);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnFiveSecond = findViewById(R.id.btnFiveSecond);

        createNotificationChannel();
        requestNotificationPermission();

        findViewById(R.id.taskChip).setOnClickListener(v ->
                startActivity(new Intent(this, TaskInboxActivity.class)));
        findViewById(R.id.btnReset).setOnClickListener(v -> resetTimer());
        btnPlay.setOnClickListener(v -> onPlayClicked());
        btnPause.setOnClickListener(v -> pauseTimer());
        btnFiveSecond.setOnClickListener(v -> openFiveSecond());

        updateTimerUi();
        updateControls();

        if (getIntent().getBooleanExtra(Extras.AUTO_START, false)) {
            startTimer();
        }
    }

    private void onPlayClicked() {
        if (running) {
            pauseTimer();
            return;
        }
        if (remainingMs == 0) {
            remainingMs = SESSION_MS;
            updateTimerUi();
        }
        if (remainingMs < SESSION_MS) {
            startTimer();
        } else {
            openFiveSecond();
        }
    }

    private void startTimer() {
        if (remainingMs <= 0) {
            return;
        }
        if (timer != null) {
            timer.cancel();
        }
        running = true;
        updateControls();
        timer = new CountDownTimer(remainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                updateTimerUi();
            }

            @Override
            public void onFinish() {
                remainingMs = 0;
                running = false;
                updateTimerUi();
                updateControls();
                onSessionComplete();
            }
        }.start();
    }

    private void pauseTimer() {
        if (!running) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        running = false;
        updateControls();
    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        running = false;
        remainingMs = SESSION_MS;
        updateTimerUi();
        updateControls();
    }

    private void updateTimerUi() {
        int totalSeconds = (int) Math.ceil(remainingMs / 1000.0);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        timerText.setText(String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds));

        int progress = (int) Math.round(remainingMs * 100.0 / SESSION_MS);
        ringProgress.setProgressCompat(progress, true);
    }

    private void updateControls() {
        boolean idle = !running && remainingMs == SESSION_MS;
        btnPlay.setImageResource(running ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        btnPlay.setContentDescription(getString(
                running ? R.string.cd_pause_timer : R.string.cd_start_timer));
        btnPause.setEnabled(running);
        btnFiveSecond.setEnabled(idle);
    }

    private void onSessionComplete() {
        showCompletionDialog();
        postCompletionNotification();
    }

    private void showCompletionDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.session_complete_title)
                .setMessage(R.string.session_complete_message)
                .setCancelable(false)
                .setPositiveButton(R.string.session_start_another, (dialog, which) -> {
                    remainingMs = SESSION_MS;
                    updateTimerUi();
                    startTimer();
                })
                .setNegativeButton(R.string.session_back_to_task, (dialog, which) -> {
                    if (taskId != null) {
                        Intent intent = new Intent(this, TaskDetailActivity.class);
                        intent.putExtra(Extras.TASK_ID, taskId);
                        intent.putExtra(Extras.TASK_TITLE, taskTitle);
                        startActivity(intent);
                    }
                    finish();
                })
                .show();
    }

    @SuppressLint("MissingPermission")
    private void postCompletionNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String label = taskTitle == null ? getString(R.string.focus_task_default) : taskTitle;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text_format, label, 25))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pending);

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATIONS);
        }
    }

    private void openFiveSecond() {
        if (running) {
            return;
        }
        Intent intent = new Intent(this, FiveSecondActivity.class);
        if (taskTitle != null) {
            intent.putExtra(Extras.TASK_ID, taskId);
            intent.putExtra(Extras.TASK_TITLE, taskTitle);
        }
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_focus;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_pomodoro;
    }
}
