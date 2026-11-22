package itech3106.focusloop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import itech3106.focusloop.data.Dates;
import itech3106.focusloop.data.PlanStore;
import itech3106.focusloop.data.SessionStore;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.data.TaskStore;
import itech3106.focusloop.data.TimeSlot;
import itech3106.focusloop.focus.FocusHistoryActivity;
import itech3106.focusloop.focus.PomodoroActivity;
import itech3106.focusloop.planning.TimeBlocksActivity;
import itech3106.focusloop.tasks.TaskDetailActivity;
import itech3106.focusloop.tasks.TaskInboxActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindChrome(0);

        ((TextView) findViewById(R.id.dateLabel))
                .setText(Dates.longDate(Dates.today()).toUpperCase(Locale.ENGLISH));

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greeting = hour < 12
                ? R.string.main_greeting
                : hour < 18 ? R.string.main_greeting_afternoon : R.string.main_greeting_evening;
        ((TextView) findViewById(R.id.greeting)).setText(greeting);

        showFrog();
        showNextBlock();
        showFocusToday();

        findViewById(R.id.nextCard).setOnClickListener(v ->
                startActivity(new Intent(this, TimeBlocksActivity.class)));
        findViewById(R.id.focusCard).setOnClickListener(v ->
                startActivity(new Intent(this, FocusHistoryActivity.class)));
        findViewById(R.id.moduleTasks).setOnClickListener(v ->
                startActivity(new Intent(this, TaskInboxActivity.class)));
        findViewById(R.id.moduleFocus).setOnClickListener(v ->
                startActivity(new Intent(this, PomodoroActivity.class)));
        findViewById(R.id.modulePlan).setOnClickListener(v ->
                startActivity(new Intent(this, TimeBlocksActivity.class)));
    }

    private void showFrog() {
        Task frog = new TaskStore(this).frog();
        TextView title = findViewById(R.id.frogTitle);
        TextView meta = findViewById(R.id.frogMeta);
        View card = findViewById(R.id.frogCard);
        View start = findViewById(R.id.frogStart);

        if (frog == null) {
            title.setText(R.string.main_frog_empty);
            meta.setText("");
            card.setOnClickListener(v ->
                    startActivity(new Intent(this, TaskInboxActivity.class)));
            start.setOnClickListener(v ->
                    startActivity(new Intent(this, TaskInboxActivity.class)));
            return;
        }

        title.setText(frog.title);
        meta.setText(frog.priority + " · " + frog.due);
        card.setOnClickListener(v -> openTask(frog));
        start.setOnClickListener(v -> {
            Intent intent = new Intent(this, PomodoroActivity.class);
            intent.putExtra(Extras.TASK_ID, frog.id);
            intent.putExtra(Extras.TASK_TITLE, frog.title);
            startActivity(intent);
        });
    }

    private void showNextBlock() {
        List<TimeSlot> slots = new PlanStore(this).forDay(Dates.today());
        String now = String.format(Locale.ENGLISH, "%02d:00",
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY));

        TimeSlot next = null;
        for (TimeSlot slot : slots) {
            if (slot.title != null && slot.hour.compareTo(now) >= 0) {
                next = slot;
                break;
            }
        }

        TextView time = findViewById(R.id.nextTime);
        TextView title = findViewById(R.id.nextTitle);
        if (next == null) {
            time.setText(R.string.main_next_empty_time);
            title.setText(R.string.main_next_empty);
            return;
        }

        String meta = next.meta == null ? next.hour : next.meta;
        int end = meta.indexOf(" · ");
        time.setText(end > 0 ? meta.substring(0, end) : meta);
        title.setText(next.title);
    }

    private void showFocusToday() {
        SessionStore sessions = new SessionStore(this);
        int minutes = sessions.minutesToday();
        int count = sessions.today().size();

        ((TextView) findViewById(R.id.focusMinutes)).setText(String.valueOf(minutes));
        ((TextView) findViewById(R.id.focusSessions)).setText(
                getResources().getQuantityString(R.plurals.main_focus_sessions_count,
                        count, count));
    }

    private void openTask(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(Extras.TASK_ID, task.id);
        startActivity(intent);
    }
}
