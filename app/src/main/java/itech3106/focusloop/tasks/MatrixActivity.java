package itech3106.focusloop.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.DemoData;
import itech3106.focusloop.data.Task;

public class MatrixActivity extends ModuleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix);
        bindChrome(R.string.title_matrix);

        findViewById(R.id.frogCard).setOnClickListener(v -> open("t1"));
        findViewById(R.id.q1Task1).setOnClickListener(v -> open("t1"));
        findViewById(R.id.q1Task2).setOnClickListener(v -> open("t2"));
        findViewById(R.id.q2Task1).setOnClickListener(v -> open("t3"));
        findViewById(R.id.q2Task2).setOnClickListener(v -> open("t5"));
        findViewById(R.id.q3Task1).setOnClickListener(v -> open("t4"));
        findViewById(R.id.q4Task1).setOnClickListener(v -> open("t6"));
        findViewById(R.id.q4Task2).setOnClickListener(v -> open("t7"));
    }

    private void open(String taskId) {
        Task task = DemoData.task(taskId);
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(Extras.TASK_ID, task.id);
        intent.putExtra(Extras.TASK_TITLE, task.title);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean created = super.onCreateOptionsMenu(menu);
        // Sorting belongs to the inbox, not to the matrix board.
        menu.removeItem(R.id.action_sort_priority);
        menu.removeItem(R.id.action_sort_due);
        return created;
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_tasks;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_matrix;
    }
}
