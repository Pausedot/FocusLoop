package itech3106.focusloop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import itech3106.focusloop.focus.FocusHistoryActivity;
import itech3106.focusloop.planning.TimeBlocksActivity;
import itech3106.focusloop.tasks.TaskDetailActivity;
import itech3106.focusloop.tasks.TaskInboxActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindChrome(0);

        TextView date = findViewById(R.id.dateLabel);
        date.setText(new SimpleDateFormat("EEEE · d MMMM", Locale.ENGLISH).format(new Date()));

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int greeting = hour < 12
                ? R.string.main_greeting
                : hour < 18 ? R.string.main_greeting_afternoon : R.string.main_greeting_evening;
        ((TextView) findViewById(R.id.greeting)).setText(greeting);

        findViewById(R.id.frogCard).setOnClickListener(v -> openFrog());
        findViewById(R.id.frogStart).setOnClickListener(v -> openFrog());
        findViewById(R.id.nextCard).setOnClickListener(v ->
                startActivity(new Intent(this, TimeBlocksActivity.class)));
        findViewById(R.id.focusCard).setOnClickListener(v ->
                startActivity(new Intent(this, FocusHistoryActivity.class)));
        findViewById(R.id.moduleTasks).setOnClickListener(v ->
                startActivity(new Intent(this, TaskInboxActivity.class)));
        findViewById(R.id.moduleFocus).setOnClickListener(v ->
                startActivity(new Intent(this, itech3106.focusloop.focus.PomodoroActivity.class)));
        findViewById(R.id.modulePlan).setOnClickListener(v ->
                startActivity(new Intent(this, TimeBlocksActivity.class)));
    }

    private void openFrog() {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(Extras.TASK_ID, "t1");
        intent.putExtra(Extras.TASK_TITLE, getString(R.string.main_frog_title));
        startActivity(intent);
    }
}
