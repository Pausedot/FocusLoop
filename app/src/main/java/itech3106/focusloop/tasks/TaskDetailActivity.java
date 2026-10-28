package itech3106.focusloop.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.DemoData;
import itech3106.focusloop.data.FocusSession;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.focus.PomodoroActivity;

public class TaskDetailActivity extends ModuleActivity {

    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        bindChrome(R.string.title_task);

        String taskId = getIntent().getStringExtra(Extras.TASK_ID);
        task = DemoData.task(taskId == null ? "t1" : taskId);

        String title = getIntent().getStringExtra(Extras.TASK_TITLE);
        TextView titleView = findViewById(R.id.detailTitle);
        titleView.setText(title == null ? task.title : title);

        TextView badge = findViewById(R.id.detailBadge);
        badge.setText(String.valueOf(task.priority));
        badge.setBackgroundResource(task.priority == 'A'
                ? R.drawable.bg_badge_a
                : R.drawable.bg_badge_outline);

        TextView quadrant = findViewById(R.id.detailQuadrant);
        quadrant.setText(task.quadrant + " · " + getString(quadrantLabel(task.quadrant)));

        TextView due = findViewById(R.id.detailDue);
        due.setText(task.due);

        TextView notes = findViewById(R.id.detailNotes);
        notes.setText(task.notes);

        int sessions = 0;
        int minutes = 0;
        for (FocusSession session : DemoData.sessions()) {
            if (session.title.equals(task.title)) {
                sessions++;
                minutes += session.minutes;
            }
        }
        String focusValue = getResources().getQuantityString(
                R.plurals.detail_sessions, sessions, sessions) + " · " + minutes + " min";
        ((TextView) findViewById(R.id.detailFocusValue)).setText(focusValue);

        findViewById(R.id.btnStartFocus).setOnClickListener(v -> {
            Intent intent = new Intent(this, PomodoroActivity.class);
            intent.putExtra(Extras.TASK_ID, task.id);
            intent.putExtra(Extras.TASK_TITLE, titleView.getText().toString());
            startActivity(intent);
        });
        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            DemoData.setDone(task.id, true);
            finish();
        });
    }

    private static int quadrantLabel(String quadrant) {
        switch (quadrant) {
            case "Q2":
                return R.string.matrix_q2;
            case "Q3":
                return R.string.matrix_q3;
            case "Q4":
                return R.string.matrix_q4;
            default:
                return R.string.matrix_q1;
        }
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_task_detail;
    }
}
