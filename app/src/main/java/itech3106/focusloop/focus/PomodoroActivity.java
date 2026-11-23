package itech3106.focusloop.focus;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Locale;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.FocusSession;
import itech3106.focusloop.data.Prefs;
import itech3106.focusloop.data.SessionStore;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.data.TaskStore;
import itech3106.focusloop.tasks.TaskDetailActivity;
import itech3106.focusloop.tasks.TaskInboxActivity;

public class PomodoroActivity extends ModuleActivity {

    private static final int REQUEST_NOTIFICATIONS = 2001;

    private final BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (FocusNotifier.ACTION_PAUSE.equals(action)) {
                pauseTimer();
            } else if (FocusNotifier.ACTION_RESUME.equals(action)) {
                startTimer();
            } else if (FocusNotifier.ACTION_STOP.equals(action)) {
                resetTimer();
            }
        }
    };

    private TextView timerText;
    private CircularProgressIndicator ringProgress;
    private FloatingActionButton btnPlay;
    private MaterialButton btnPause;
    private MaterialButton btnFiveSecond;

    private CountDownTimer timer;
    private long sessionMs;
    private long remainingMs;
    private boolean running;
    private boolean receiverRegistered;
    private String taskTitle;
    private String taskId;
    private ActivityResultLauncher<Intent> taskPicker;
    private AlertDialog completionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);
        bindChrome(R.string.title_focus);

        sessionMs = Prefs.focusMinutes(this) * 60_000L;
        remainingMs = sessionMs;

        taskPicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        taskId = result.getData().getStringExtra(Extras.TASK_ID);
                        taskTitle = result.getData().getStringExtra(Extras.TASK_TITLE);
                        showTask();
                    }
                });

        taskTitle = getIntent().getStringExtra(Extras.TASK_TITLE);
        taskId = getIntent().getStringExtra(Extras.TASK_ID);
        if (taskTitle == null && Prefs.timerTaskTitle(this) != null) {
            taskTitle = Prefs.timerTaskTitle(this);
            taskId = Prefs.timerTaskId(this);
        }
        showTask();

        timerText = findViewById(R.id.timerText);
        ringProgress = findViewById(R.id.ringProgress);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnFiveSecond = findViewById(R.id.btnFiveSecond);

        FocusNotifier.ensureChannels(this);
        registerTimerReceiver();
        requestNotificationPermission();

        findViewById(R.id.taskChip).setOnClickListener(v -> {
            if (!running) {
                taskPicker.launch(new Intent(this, TaskInboxActivity.class)
                        .putExtra(Extras.PICK_TASK, true));
            }
        });
        findViewById(R.id.btnReset).setOnClickListener(v -> resetTimer());
        btnPlay.setOnClickListener(v -> onPlayClicked());
        btnPause.setOnClickListener(v -> pauseTimer());
        btnFiveSecond.setOnClickListener(v -> openFiveSecond());

        GestureDetector gestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        onPlayClicked();
                        return true;
                    }
                });
        ringProgress.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
        ringProgress.setContentDescription(getString(R.string.cd_timer_double_tap));

        updateTimerUi();
        updateControls();
        updateSessionChrome();
        restoreTimerState();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (running || (remainingMs > 0 && remainingMs < sessionMs)) {
                    new MaterialAlertDialogBuilder(PomodoroActivity.this)
                            .setTitle(R.string.session_end_title)
                            .setMessage(R.string.session_end_message)
                            .setPositiveButton(R.string.session_end_record, (d, w) -> {
                                resetTimer();
                                finish();
                            })
                            .setNegativeButton(R.string.session_keep_going, null)
                            .show();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (getIntent().getBooleanExtra(Extras.AUTO_START, false)) {
            startTimer();
        }
    }

    private void showTask() {
        TextView title = findViewById(R.id.focusTaskTitle);
        TextView meta = findViewById(R.id.focusTaskMeta);
        if (taskTitle == null) {
            title.setText(R.string.focus_task_default);
            meta.setText(R.string.focus_no_task_meta);
            return;
        }
        title.setText(taskTitle);
        Task task = taskId == null ? null : new TaskStore(this).byId(taskId);
        meta.setText(task == null
                ? getString(R.string.focus_no_task_meta)
                : task.quadrant + " · " + task.due);
    }

    private void restoreTimerState() {
        if (!Prefs.timerRunning(this)) {
            return;
        }
        long remaining = Prefs.timerEndAt(this) - System.currentTimeMillis();
        if (remaining > 0) {
            remainingMs = Math.min(remaining, sessionMs);
            startTimer();
            return;
        }
        remainingMs = 0;
        running = false;
        updateTimerUi();
        updateControls();
        Prefs.clearTimer(this);
        onSessionComplete();
    }

    private void onPlayClicked() {        if (running) {
            pauseTimer();
            return;
        }
        if (remainingMs == 0) {
            remainingMs = sessionMs;
            updateTimerUi();
        }
        if (remainingMs < sessionMs) {
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
        Prefs.saveTimerRunning(this, System.currentTimeMillis() + remainingMs, taskId, taskTitle);
        FocusNotifier.showRunning(this, taskTitle,
                System.currentTimeMillis() + remainingMs, false, remainingMs);
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
        Prefs.saveTimerPaused(this, remainingMs, taskId, taskTitle);
        FocusNotifier.showRunning(this, taskTitle, 0, true, remainingMs);
    }

    private void resetTimer() {
        boolean midSession = running || (remainingMs > 0 && remainingMs < sessionMs);
        if (midSession) {
            recordSession(false);
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        running = false;
        remainingMs = sessionMs;
        updateTimerUi();
        updateControls();
        FocusNotifier.cancelRunning(this);
        Prefs.clearTimer(this);
        updateSessionChrome();
    }

    private void updateTimerUi() {
        int totalSeconds = (int) Math.ceil(remainingMs / 1000.0);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        timerText.setText(String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds));

        int progress = sessionMs == 0 ? 0
                : (int) Math.round(remainingMs * 100.0 / sessionMs);
        ringProgress.setProgressCompat(progress, true);
    }

    private void updateControls() {
        boolean idle = !running && remainingMs == sessionMs;
        btnPlay.setImageResource(running ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        btnPlay.setContentDescription(getString(
                running ? R.string.cd_pause_timer : R.string.cd_start_timer));
        btnPause.setEnabled(running);
        btnFiveSecond.setEnabled(idle);
    }

    private void updateSessionChrome() {
        SessionStore stats = new SessionStore(this);
        int round = stats.completedToday() % 4 + 1;
        ((TextView) findViewById(R.id.sessionLabel))
                .setText(getString(R.string.focus_session_format, round));
        ((TextView) findViewById(R.id.timerCaption))
                .setText(getString(R.string.focus_caption_format, Prefs.focusMinutes(this)));
        ((TextView) findViewById(R.id.focusTodayValue))
                .setText(getString(R.string.focus_today_format,
                        stats.today().size(), stats.minutesToday()));
    }

    private void onSessionComplete() {
        if (Prefs.soundEnabled(this)) {
            Chime.play(this);
        }
        Prefs.clearTimer(this);
        recordSession(true);
        updateSessionChrome();
        showCompletionDialog();
        FocusNotifier.cancelRunning(this);
        FocusNotifier.showComplete(this, taskId, taskTitle,
                Math.max(1, Math.round(sessionMs / 60_000f)));
    }

    private void recordSession(boolean completed) {
        SessionStore store = new SessionStore(this);
        String title = taskTitle == null ? getString(R.string.focus_task_default) : taskTitle;
        long elapsed = completed ? sessionMs : Math.max(0, sessionMs - remainingMs);
        int minutes = Math.max(1, Math.round(elapsed / 60_000f));
        int round = store.completedToday() + 1;
        store.record(new FocusSession(
                "s-" + System.currentTimeMillis(),
                System.currentTimeMillis() - elapsed,
                taskId,
                title,
                minutes,
                completed,
                round));
    }

    private void showCompletionDialog() {
        completionDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.session_complete_title)
                .setMessage(R.string.session_complete_message)
                .setCancelable(false)
                .setPositiveButton(R.string.session_take_break, (dialog, which) -> openBreak())
                .setNegativeButton(R.string.session_back_to_task, (dialog, which) -> {
                    if (taskId != null) {
                        Intent intent = new Intent(this, TaskDetailActivity.class);
                        intent.putExtra(Extras.TASK_ID, taskId);
                        intent.putExtra(Extras.TASK_TITLE, taskTitle);
                        startActivity(intent);
                    }
                    finish();
                })
                .create();
        completionDialog.show();
    }

    private void openBreak() {
        Intent intent = new Intent(this, BreakActivity.class);
        intent.putExtra(Extras.TASK_ID, taskId);
        intent.putExtra(Extras.TASK_TITLE, taskTitle);
        startActivity(intent);
        finish();
    }

    private void registerTimerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FocusNotifier.ACTION_PAUSE);
        filter.addAction(FocusNotifier.ACTION_RESUME);
        filter.addAction(FocusNotifier.ACTION_STOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(timerReceiver, filter);
        }
        receiverRegistered = true;
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
    protected void onResume() {
        super.onResume();
        // The settings screen may have changed the length while this timer was idle.
        if (!running && remainingMs == sessionMs) {
            long updated = Prefs.focusMinutes(this) * 60_000L;
            if (updated != sessionMs) {
                sessionMs = updated;
                remainingMs = updated;
                updateTimerUi();
                updateControls();
            }
        }
        updateSessionChrome();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home
                && (running || (remainingMs > 0 && remainingMs < sessionMs))) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (running) {
            return;
        }
        if (completionDialog != null) {
            completionDialog.dismiss();
            completionDialog = null;
        }
        taskId = intent.getStringExtra(Extras.TASK_ID);
        taskTitle = intent.getStringExtra(Extras.TASK_TITLE);
        showTask();
        if (intent.getBooleanExtra(Extras.AUTO_START, false)) {
            sessionMs = Prefs.focusMinutes(this) * 60_000L;
            remainingMs = sessionMs;
            updateTimerUi();
            updateControls();
            startTimer();
        }
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (receiverRegistered) {
            unregisterReceiver(timerReceiver);
            receiverRegistered = false;
        }
        FocusNotifier.cancelRunning(this);
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
