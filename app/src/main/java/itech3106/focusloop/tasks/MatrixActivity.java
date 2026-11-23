package itech3106.focusloop.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.data.TaskStore;

public class MatrixActivity extends ModuleActivity {

    private TaskStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix);
        bindChrome(R.string.title_matrix);

        store = new TaskStore(this);
        List<Task> open = store.open();
        List<Task> q1 = byQuadrant(open, "Q1");
        List<Task> q2 = byQuadrant(open, "Q2");
        List<Task> q3 = byQuadrant(open, "Q3");
        List<Task> q4 = byQuadrant(open, "Q4");

        bindSlot(R.id.q1Task1, q1, 0);
        bindSlot(R.id.q1Task2, q1, 1);
        bindSlot(R.id.q2Task1, q2, 0);
        bindSlot(R.id.q2Task2, q2, 1);
        bindSlot(R.id.q3Task1, q3, 0);
        bindSlot(R.id.q4Task1, q4, 0);
        bindSlot(R.id.q4Task2, q4, 1);

        count(R.id.q1Count, q1.size());
        count(R.id.q2Count, q2.size());
        count(R.id.q3Count, q3.size());
        count(R.id.q4Count, q4.size());

        TextView more = findViewById(R.id.q4More);
        if (q4.size() > 2) {
            more.setVisibility(View.VISIBLE);
            more.setText(getString(R.string.matrix_more_format, q4.size() - 2));
            more.setOnClickListener(v -> open(q4.get(2).id));
        } else {
            more.setVisibility(View.GONE);
        }

        Task frog = store.frog();
        TextView frogTitle = findViewById(R.id.frogTitle);
        TextView frogMeta = findViewById(R.id.frogMeta);
        if (frog == null) {
            frogTitle.setText(R.string.matrix_frog_empty);
            frogMeta.setText(R.string.matrix_frog_meta_empty);
            findViewById(R.id.frogCard).setOnClickListener(v ->
                    startActivity(new Intent(this, TaskInboxActivity.class)));
        } else {
            frogTitle.setText(frog.title);
            frogMeta.setText(frog.priority + " · " + frog.due);
            findViewById(R.id.frogCard).setOnClickListener(v -> open(frog.id));
        }
    }

    private static List<Task> byQuadrant(List<Task> tasks, String quadrant) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (quadrant.equals(task.quadrant)) {
                result.add(task);
            }
        }
        return result;
    }

    private void bindSlot(int viewId, List<Task> tasks, int index) {
        TextView view = findViewById(viewId);
        if (index < tasks.size()) {
            Task task = tasks.get(index);
            view.setVisibility(View.VISIBLE);
            view.setText(task.title);
            view.setOnClickListener(v -> open(task.id));
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void count(int viewId, int size) {
        ((TextView) findViewById(viewId)).setText(
                getResources().getQuantityString(R.plurals.matrix_task_count, size, size));
    }

    private void open(String taskId) {
        Task task = taskId == null ? null : store.byId(taskId);
        if (task == null) {
            return;
        }
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(Extras.TASK_ID, task.id);
        intent.putExtra(Extras.TASK_TITLE, task.title);
        startActivity(intent);
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_tasks;
    }

    @Override
    protected boolean showAction(int id) {
        // Sorting belongs to the inbox, not to the matrix board.
        return id != R.id.action_sort_priority && id != R.id.action_sort_due;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_matrix;
    }
}
