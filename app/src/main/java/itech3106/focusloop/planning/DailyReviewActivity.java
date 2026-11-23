package itech3106.focusloop.planning;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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
                previous == null ? null : previous.voicePath,
                System.currentTimeMillis());
        reviewStore.save(review);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("step", step);
        outState.putStringArray("answers", answers);
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
