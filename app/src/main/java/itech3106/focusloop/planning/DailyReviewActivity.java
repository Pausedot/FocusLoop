package itech3106.focusloop.planning;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

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

    private ReviewStore reviewStore;
    private int today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_review);
        bindChrome(R.string.title_review);

        today = Dates.today();
        reviewStore = new ReviewStore(this);

        ((TextView) findViewById(R.id.reviewDate)).setText(
                Dates.longDate(today).toUpperCase(Locale.ENGLISH));

        showTiles();
        showExistingReview();

        findViewById(R.id.btnSaveReview).setOnClickListener(v -> save());
        findViewById(R.id.btnViewStats).setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));
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

    private void showExistingReview() {
        Review review = reviewStore.forDay(today);
        if (review == null) {
            return;
        }
        ((TextInputEditText) findViewById(R.id.q1Edit)).setText(review.wentWell);
        ((TextInputEditText) findViewById(R.id.q2Edit)).setText(review.obstacle);
        ((TextInputEditText) findViewById(R.id.q3Edit)).setText(review.tomorrow);
    }

    private void save() {
        String wentWell = text(R.id.q1Edit);
        String obstacle = text(R.id.q2Edit);
        String tomorrow = text(R.id.q3Edit);

        ReviewStore store = new ReviewStore(this);
        Review previous = store.forDay(today);
        Review review = new Review(
                "r-" + today,
                today,
                wentWell,
                obstacle,
                tomorrow,
                previous == null ? 0 : previous.mood,
                previous == null ? null : previous.voicePath,
                System.currentTimeMillis());
        store.save(review);
        finish();
    }

    private String text(int viewId) {
        TextInputEditText edit = findViewById(viewId);
        return edit.getText() == null ? "" : edit.getText().toString().trim();
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
