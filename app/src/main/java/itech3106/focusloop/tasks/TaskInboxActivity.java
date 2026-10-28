package itech3106.focusloop.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.adapter.TaskAdapter;
import itech3106.focusloop.data.DemoData;
import itech3106.focusloop.data.Task;

public class TaskInboxActivity extends ModuleActivity {

    private final List<Task> tasks = new ArrayList<>();
    private final List<Object> rows = new ArrayList<>();

    private TaskAdapter adapter;
    private RecyclerView list;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_inbox);
        bindChrome(R.string.title_tasks);

        tasks.addAll(DemoData.tasks());
        rows.addAll(DemoData.groupRows(tasks));

        list = findViewById(R.id.taskList);
        emptyState = findViewById(R.id.emptyState);
        adapter = new TaskAdapter(rows, new TaskAdapter.OnTaskClick() {
            @Override
            public void onTaskClick(Task task) {
                openDetail(task);
            }

            @Override
            public void onTaskLongClick(Task task) {
                confirmDelete(task);
            }
        });
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.fabAdd).setOnClickListener(v -> openEditor());
        findViewById(R.id.emptyAction).setOnClickListener(v -> openEditor());

        updateSummary();
        updateEmptyState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter == null) {
            return;
        }
        tasks.clear();
        tasks.addAll(DemoData.tasks());
        rebuildRows();
        updateSummary();
    }

    private void openDetail(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(Extras.TASK_ID, task.id);
        intent.putExtra(Extras.TASK_TITLE, task.title);
        startActivity(intent);
    }

    private void openEditor() {
        startActivity(new Intent(this, TaskEditActivity.class));
    }

    private void confirmDelete(Task task) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_task_title)
                .setMessage(getString(R.string.delete_task_body, task.title))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    tasks.remove(task);
                    rebuildRows();
                    updateSummary();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void updateSummary() {
        int open = 0;
        int dueToday = 0;
        for (Task task : tasks) {
            if (!task.done) {
                open++;
                if ("Today".equals(task.due)) {
                    dueToday++;
                }
            }
        }
        TextView summary = findViewById(R.id.inboxSummary);
        summary.setText(getString(R.string.inbox_summary_format, open, dueToday));
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        list.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void rebuildRows() {
        rows.clear();
        rows.addAll(DemoData.groupRows(tasks));
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private static int dueRank(Task task) {
        switch (task.due) {
            case "Today":
                return 0;
            case "Tomorrow":
                return 1;
            case "No date":
                return 4;
            default:
                return 2;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort_priority) {
            Collections.sort(tasks, Comparator.comparingInt(task -> task.priority));
            rebuildRows();
            return true;
        }
        if (id == R.id.action_sort_due) {
            Collections.sort(tasks, Comparator.comparingInt(TaskInboxActivity::dueRank));
            rebuildRows();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_tasks;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_inbox;
    }
}
