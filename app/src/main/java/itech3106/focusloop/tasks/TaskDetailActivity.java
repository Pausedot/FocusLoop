package itech3106.focusloop.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.SessionStore;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.data.TaskStore;
import itech3106.focusloop.focus.PomodoroActivity;

public class TaskDetailActivity extends ModuleActivity {

    private Task task;
    private TaskStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        bindChrome(R.string.title_task);

        store = new TaskStore(this);
        String taskId = getIntent().getStringExtra(Extras.TASK_ID);
        task = taskId == null ? null : store.byId(taskId);
        if (task == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.detailTitle)).setText(task.title);

        TextView badge = findViewById(R.id.detailBadge);
        badge.setText(String.valueOf(task.priority));
        badge.setBackgroundResource(task.priority == 'A'
                ? R.drawable.bg_badge_a
                : R.drawable.bg_badge_outline);

        TextView quadrant = findViewById(R.id.detailQuadrant);
        quadrant.setText(task.quadrant + " · " + getString(quadrantLabel(task.quadrant)));

        ((TextView) findViewById(R.id.detailDue)).setText(task.due);
        ((TextView) findViewById(R.id.detailNotes)).setText(task.notes);

        SessionStore sessions = new SessionStore(this);
        int count = sessions.countForTask(task.id);
        int minutes = sessions.minutesForTask(task.id);
        String focusValue = getResources().getQuantityString(
                R.plurals.detail_sessions, count, count) + " · " + minutes + " min";
        ((TextView) findViewById(R.id.detailFocusValue)).setText(focusValue);

        findViewById(R.id.btnStartFocus).setOnClickListener(v -> {
            Intent intent = new Intent(this, PomodoroActivity.class);
            intent.putExtra(Extras.TASK_ID, task.id);
            intent.putExtra(Extras.TASK_TITLE, task.title);
            startActivity(intent);
        });
        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            store.setDone(task.id, true);
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
