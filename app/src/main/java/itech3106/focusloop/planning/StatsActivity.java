package itech3106.focusloop.planning;

import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;

public class StatsActivity extends ModuleActivity {

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
        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_YEAR, weekOffset * 7);
        int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
        int shiftToMonday = (dayOfWeek + 5) % 7;
        day.add(Calendar.DAY_OF_YEAR, -shiftToMonday);

        Calendar start = (Calendar) day.clone();
        day.add(Calendar.DAY_OF_YEAR, 6);
        Calendar end = (Calendar) day.clone();

        SimpleDateFormat format = new SimpleDateFormat("d MMM", Locale.ENGLISH);
        ((TextView) findViewById(R.id.weekText)).setText(
                format.format(start.getTime()) + " – " + format.format(end.getTime()));

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
