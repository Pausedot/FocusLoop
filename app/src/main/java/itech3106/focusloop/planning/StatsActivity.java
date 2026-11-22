package itech3106.focusloop.planning;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.Dates;
import itech3106.focusloop.data.FocusSession;
import itech3106.focusloop.data.PlanStore;
import itech3106.focusloop.data.SessionStore;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.data.TaskStore;

public class StatsActivity extends ModuleActivity {

    private static final int FOCUS_TARGET_MIN = 600;
    private static final int SESSION_TARGET = 20;
    private static final int TASK_TARGET = 20;

    private int weekOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        bindChrome(R.string.title_stats);

        findViewById(R.id.btnPrevWeek).setOnClickListener(v -> {
            weekOffset--;
            updateWeek();
        });
        findViewById(R.id.btnNextWeek).setOnClickListener(v -> {
            weekOffset++;
            updateWeek();
        });
        updateWeek();
    }

    private void updateWeek() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
        LocalDate sunday = monday.plusDays(6);
        int startDay = (int) monday.toEpochDay();
        int endDay = (int) sunday.toEpochDay();
        long startMs = Dates.startOfDay(startDay);
        long endMs = Dates.startOfDay(endDay + 1);

        DateTimeFormatter format = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
        ((TextView) findViewById(R.id.weekText)).setText(
                format.format(monday) + " – " + format.format(sunday));

        int caption;
        if (weekOffset == 0) {
            caption = R.string.stats_this_week;
        } else if (weekOffset == -1) {
            caption = R.string.stats_last_week;
        } else if (weekOffset == 1) {
            caption = R.string.stats_next_week;
        } else {
            caption = R.string.stats_selected_week;
        }
        ((TextView) findViewById(R.id.weekCaption)).setText(caption);

        List<FocusSession> sessions = new SessionStore(this).all();
        int focusMinutes = 0;
        int sessionCount = 0;
        int deepMinutes = 0;
        for (FocusSession session : sessions) {
            if (session.startedAt < startMs || session.startedAt >= endMs) {
                continue;
            }
            focusMinutes += session.minutes;
            sessionCount++;
            if (session.minutes >= 25) {
                deepMinutes += session.minutes;
            }
        }

        int tasksDone = 0;
        for (Task task : new TaskStore(this).all()) {
            if (task.done && task.completedAt >= startMs && task.completedAt < endMs) {
                tasksDone++;
            }
        }

        int planned = 0;
        PlanStore plan = new PlanStore(this);
        for (int day = startDay; day <= endDay; day++) {
            planned += plan.plannedMinutes(day);
        }
        int accuracy = planned == 0 ? 0 : Math.min(100, focusMinutes * 100 / planned);
        int deep = focusMinutes == 0 ? 0 : deepMinutes * 100 / focusMinutes;

        ((TextView) findViewById(R.id.focusTimeValue)).setText(duration(focusMinutes));
        ((TextView) findViewById(R.id.sessionsValue)).setText(String.valueOf(sessionCount));
        ((TextView) findViewById(R.id.tasksValue)).setText(String.valueOf(tasksDone));
        ((TextView) findViewById(R.id.accuracyValue))
                .setText(getString(R.string.stats_percent_format, accuracy));
        ((TextView) findViewById(R.id.deepValue))
                .setText(getString(R.string.stats_percent_format, deep));

        bar(R.id.focusTimeBar, focusMinutes, FOCUS_TARGET_MIN);
        bar(R.id.sessionsBar, sessionCount, SESSION_TARGET);
        bar(R.id.tasksBar, tasksDone, TASK_TARGET);
        bar(R.id.accuracyBar, accuracy, 100);
        bar(R.id.deepBar, deep, 100);

        ((TextView) findViewById(R.id.totalValue)).setText(
                getString(R.string.stats_total_value_format, duration(focusMinutes)));

        int previous = focusMinutesForWeek(weekOffset - 1);
        TextView delta = findViewById(R.id.totalDelta);
        if (previous == 0) {
            delta.setText(R.string.stats_delta_none);
        } else {
            int change = Math.round((focusMinutes - previous) * 100f / previous);
            if (change > 0) {
                delta.setText(getString(R.string.stats_delta_up, change));
            } else if (change < 0) {
                delta.setText(getString(R.string.stats_delta_down, Math.abs(change)));
            } else {
                delta.setText(R.string.stats_delta_same);
            }
        }
    }

    private int focusMinutesForWeek(int offset) {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(offset);
        long start = Dates.startOfDay((int) monday.toEpochDay());
        long end = Dates.startOfDay((int) monday.plusDays(7).toEpochDay());
        int minutes = 0;
        for (FocusSession session : new SessionStore(this).all()) {
            if (session.startedAt >= start && session.startedAt < end) {
                minutes += session.minutes;
            }
        }
        return minutes;
    }

    private void bar(int id, int value, int target) {
        ProgressBar bar = findViewById(id);
        bar.setProgress(target == 0 ? 0 : Math.min(100, value * 100 / target));
    }

    private String duration(int minutes) {
        if (minutes < 60) {
            return getString(R.string.stats_duration_m, minutes);
        }
        return getString(R.string.stats_duration_hm, minutes / 60, minutes % 60);
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_plan;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_stats;
    }
}
