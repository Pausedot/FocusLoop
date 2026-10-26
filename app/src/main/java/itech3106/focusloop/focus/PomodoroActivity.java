package itech3106.focusloop.focus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.DemoData;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.tasks.TaskInboxActivity;

public class PomodoroActivity extends ModuleActivity {

    private String taskTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);
        bindChrome(R.string.title_focus);

        taskTitle = getIntent().getStringExtra(Extras.TASK_TITLE);
        TextView title = findViewById(R.id.focusTaskTitle);
        TextView meta = findViewById(R.id.focusTaskMeta);
        if (taskTitle == null) {
            title.setText(R.string.focus_task_default);
            meta.setText(R.string.focus_no_task_meta);
        } else {
            title.setText(taskTitle);
            String taskId = getIntent().getStringExtra(Extras.TASK_ID);
            Task task = DemoData.task(taskId == null ? "t1" : taskId);
            meta.setText(task.quadrant + " · " + task.due);
        }

        findViewById(R.id.taskChip).setOnClickListener(v ->
                startActivity(new Intent(this, TaskInboxActivity.class)));
        findViewById(R.id.btnPlay).setOnClickListener(v -> openFiveSecond());
        findViewById(R.id.btnFiveSecond).setOnClickListener(v -> openFiveSecond());
        findViewById(R.id.btnReset).setOnClickListener(v ->
                Toast.makeText(this, R.string.focus_reset_toast, Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnPause).setOnClickListener(v ->
                Toast.makeText(this, R.string.focus_pause_toast, Toast.LENGTH_SHORT).show());
    }

    private void openFiveSecond() {
        Intent intent = new Intent(this, FiveSecondActivity.class);
        if (taskTitle != null) {
            intent.putExtra(Extras.TASK_ID, getIntent().getStringExtra(Extras.TASK_ID));
            intent.putExtra(Extras.TASK_TITLE, taskTitle);
        }
        startActivity(intent);
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
