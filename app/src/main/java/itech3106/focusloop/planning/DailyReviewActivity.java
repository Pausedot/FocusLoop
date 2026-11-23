package itech3106.focusloop.planning;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.Dates;
import itech3106.focusloop.data.PlanStore;
import itech3106.focusloop.data.Review;
import itech3106.focusloop.data.ReviewStore;
import itech3106.focusloop.data.SessionStore;
import itech3106.focusloop.data.TaskStore;

public class DailyReviewActivity extends ModuleActivity {

    private final String[] answers = new String[3];

    private ReviewStore reviewStore;
    private int today;
    private int step;
    private boolean twoPane;

    private static final int REQUEST_AUDIO = 4001;

    private MediaRecorder recorder;
    private MediaPlayer player;
    private File voiceFile;
    private String voicePath;
    private boolean recording;
    private TextView btnVoice;
    private View btnPlayVoice;
    private TextView voiceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_review);
        bindChrome(R.string.title_review);

        today = Dates.today();
        reviewStore = new ReviewStore(this);
        ((TextView) findViewById(R.id.reviewDate))
                .setText(Dates.longDate(today).toUpperCase(Locale.ENGLISH));

        loadExisting();
        showTiles();
        if (savedInstanceState != null) {
            voicePath = savedInstanceState.getString("voice");
        }
        setupVoice();

        twoPane = findViewById(R.id.step0) != null;
        if (twoPane) {
            setupTwoPane(savedInstanceState);
        } else {
            setupPhone(savedInstanceState);
        }
    }

    private void loadExisting() {
        Review review = reviewStore.forDay(today);
        if (review == null) {
            return;
        }
        answers[0] = review.wentWell;
        answers[1] = review.obstacle;
        answers[2] = review.tomorrow;
        voicePath = review.voicePath;
    }

    private void showTiles() {
        int sessions = new SessionStore(this).completedToday();
        int tasks = new TaskStore(this).completedOn(today).size();
        float hours = new PlanStore(this).plannedMinutes(today) / 60f;

        ((TextView) findViewById(R.id.tileSessionsValue)).setText(String.valueOf(sessions));
        ((TextView) findViewById(R.id.tileTasksValue)).setText(String.valueOf(tasks));
        ((TextView) findViewById(R.id.tileHoursValue))
                .setText(String.format(Locale.ENGLISH, "%.1f", hours));
    }

    private void setupPhone(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            step = savedInstanceState.getInt("step", 0);
            String[] saved = savedInstanceState.getStringArray("answers");
            if (saved != null) {
                System.arraycopy(saved, 0, answers, 0, Math.min(saved.length, answers.length));
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (step > 0) {
                showStep(step - 1);
            }
        });
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            if (step < 2) {
                showStep(step + 1);
            } else {
                saveReview();
            }
        });
        showStep(step);
    }

    private void setupTwoPane(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            addStep(R.id.step0, 0, "step0");
            addStep(R.id.step1, 1, "step1");
            addStep(R.id.step2, 2, "step2");
        }
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            collectTwoPane();
            saveReview();
        });
    }

    private void addStep(int containerId, int index, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, ReviewStepFragment.newInstance(index, answers[index]), tag)
                .commit();
    }

    private void showStep(int newStep) {
        Fragment current = getSupportFragmentManager().findFragmentByTag("step");
        if (current instanceof ReviewStepFragment) {
            answers[step] = ((ReviewStepFragment) current).answer();
        }
        step = newStep;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.stepContainer,
                        ReviewStepFragment.newInstance(step, answers[step]), "step")
                .commit();
        updateStepChrome();
    }

    private void updateStepChrome() {
        ((View) findViewById(R.id.btnBack)).setEnabled(step > 0);
        ((TextView) findViewById(R.id.btnNext)).setText(
                step == 2 ? R.string.action_save : R.string.review_next);

        int accent = R.drawable.bg_dot_accent;
        int muted = R.drawable.bg_dot_muted;
        findViewById(R.id.dot1).setBackgroundResource(step == 0 ? accent : muted);
        findViewById(R.id.dot2).setBackgroundResource(step == 1 ? accent : muted);
        findViewById(R.id.dot3).setBackgroundResource(step == 2 ? accent : muted);
    }

    private void collectTwoPane() {
        Fragment zero = getSupportFragmentManager().findFragmentByTag("step0");
        Fragment one = getSupportFragmentManager().findFragmentByTag("step1");
        Fragment two = getSupportFragmentManager().findFragmentByTag("step2");
        if (zero instanceof ReviewStepFragment) {
            answers[0] = ((ReviewStepFragment) zero).answer();
        }
        if (one instanceof ReviewStepFragment) {
            answers[1] = ((ReviewStepFragment) one).answer();
        }
        if (two instanceof ReviewStepFragment) {
            answers[2] = ((ReviewStepFragment) two).answer();
        }
    }

    private void saveReview() {
        Fragment current = getSupportFragmentManager().findFragmentByTag("step");
        if (current instanceof ReviewStepFragment) {
            answers[step] = ((ReviewStepFragment) current).answer();
        }

        Review previous = reviewStore.forDay(today);
        Review review = new Review(
                "r-" + today,
                today,
                answers[0] == null ? "" : answers[0],
                answers[1] == null ? "" : answers[1],
                answers[2] == null ? "" : answers[2],
                previous == null ? 0 : previous.mood,
                voicePath,
                System.currentTimeMillis());
        reviewStore.save(review);
        finish();
    }

    private void setupVoice() {
        btnVoice = findViewById(R.id.btnVoice);
        btnPlayVoice = findViewById(R.id.btnPlayVoice);
        voiceStatus = findViewById(R.id.voiceStatus);
        btnVoice.setOnClickListener(v -> {
            if (recording) {
                stopRecording();
            } else {
                startRecording();
            }
        });
        btnPlayVoice.setOnClickListener(v -> playVoice());
        updateVoiceUi();
    }

    private void startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
            return;
        }

        File dir = new File(getFilesDir(), "voice");
        if (!dir.exists() && !dir.mkdirs()) {
            return;
        }
        voiceFile = new File(dir, "review-" + today + ".m4a");
        if (voiceFile.exists()) {
            voiceFile.delete();
        }

        recorder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? new MediaRecorder(this)
                : new MediaRecorder();
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(64000);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(voiceFile.getAbsolutePath());
            recorder.setMaxDuration(120000);
            recorder.setOnInfoListener((mr, what, extra) -> {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording();
                }
            });
            recorder.prepare();
            recorder.start();
            recording = true;
            updateVoiceUi();
        } catch (IOException | RuntimeException e) {
            releaseRecorder();
            recording = false;
            Toast.makeText(this, R.string.voice_permission, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (recorder == null) {
            return;
        }
        try {
            recorder.stop();
        } catch (RuntimeException e) {
            if (voiceFile != null) {
                voiceFile.delete();
            }
        }
        releaseRecorder();
        recording = false;
        if (voiceFile != null && voiceFile.exists() && voiceFile.length() > 0) {
            voicePath = voiceFile.getAbsolutePath();
        }
        updateVoiceUi();
    }

    private void releaseRecorder() {
        if (recorder != null) {
            try {
                recorder.reset();
            } catch (RuntimeException ignored) {
                // Already stopped or released.
            }
            recorder.release();
            recorder = null;
        }
    }

    private void playVoice() {
        if (voicePath == null) {
            return;
        }
        try {
            if (player != null) {
                player.release();
            }
            player = new MediaPlayer();
            player.setDataSource(voicePath);
            player.setOnCompletionListener(mp -> {
                mp.release();
                if (player == mp) {
                    player = null;
                }
            });
            player.prepare();
            player.start();
        } catch (IOException | RuntimeException e) {
            if (player != null) {
                player.release();
                player = null;
            }
        }
    }

    private void updateVoiceUi() {
        if (btnVoice == null) {
            return;
        }
        btnVoice.setText(recording ? R.string.voice_stop : R.string.voice_record);
        btnPlayVoice.setVisibility(voicePath == null ? View.GONE : View.VISIBLE);
        voiceStatus.setText(recording
                ? R.string.voice_recording
                : voicePath == null ? R.string.voice_none : R.string.voice_recorded);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_AUDIO) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            Toast.makeText(this, R.string.voice_permission, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        if (recording) {
            stopRecording();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        releaseRecorder();
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("step", step);
        outState.putStringArray("answers", answers);
        outState.putString("voice", voicePath);
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_plan;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_daily_review;
    }
}
