package itech3106.focusloop.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

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
import itech3106.focusloop.data.TaskStore;
import itech3106.focusloop.reminder.ReminderScheduler;

public class TaskInboxActivity extends ModuleActivity implements TaskDetailFragment.Listener {

    private final List<Task> tasks = new ArrayList<>();
    private final List<Object> rows = new ArrayList<>();

    private TaskStore store;
    private TaskAdapter adapter;
    private RecyclerView list;
    private View emptyState;
    private FrameLayout detailContainer;
    private View detailPlaceholder;
    private boolean pickMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_inbox);
        pickMode = getIntent().getBooleanExtra(Extras.PICK_TASK, false);
        bindChrome(pickMode ? R.string.title_pick_task : R.string.title_tasks);

        store = new TaskStore(this);
        detailContainer = findViewById(R.id.detailContainer);
        detailPlaceholder = findViewById(R.id.detailPlaceholder);

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

            @Override
            public void onTaskChecked(Task task, boolean checked) {
                store.setDone(task.id, checked);
                load();
                if (checked && isShowing(task.id)) {
                    clearDetail();
                }
            }
        });
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION || rows.get(position) instanceof String) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION
                        || !(rows.get(position) instanceof Task)) {
                    adapter.notifyDataSetChanged();
                    return;
                }
                Task task = (Task) rows.get(position);
                if (direction == ItemTouchHelper.RIGHT) {
                    store.setDone(task.id, true);
                    load();
                    Snackbar.make(list, R.string.task_completed, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_undo, v -> {
                                store.setDone(task.id, false);
                                load();
                            })
                            .show();
                } else {
                    store.remove(task.id);
                    load();
                    Snackbar.make(list, R.string.task_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_undo, v -> {
                                store.add(task);
                                load();
                            })
                            .show();
                }
                if (isShowing(task.id)) {
                    clearDetail();
                }
            }
        }).attachToRecyclerView(list);

        findViewById(R.id.fabAdd).setOnClickListener(v -> openEditor());
        findViewById(R.id.emptyAction).setOnClickListener(v -> openEditor());
        if (pickMode) {
            findViewById(R.id.fabAdd).setVisibility(View.GONE);
            findViewById(R.id.emptyAction).setVisibility(View.GONE);
        }

        if (detailContainer != null
                && getSupportFragmentManager().findFragmentByTag("detail") != null
                && detailPlaceholder != null) {
            detailPlaceholder.setVisibility(View.GONE);
        }

        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter == null) {
            return;
        }
        load();
        ReminderScheduler.scheduleTasks(this);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("detail");
        if (fragment instanceof TaskDetailFragment) {
            ((TaskDetailFragment) fragment).reload();
        }
    }

    private void load() {
        tasks.clear();
        tasks.addAll(store.open());
        rebuildRows();
        updateSummary();
    }

    private void openDetail(Task task) {
        if (pickMode) {
            Intent data = new Intent();
            data.putExtra(Extras.TASK_ID, task.id);
            data.putExtra(Extras.TASK_TITLE, task.title);
            setResult(RESULT_OK, data);
            finish();
            return;
        }
        if (detailContainer == null) {
            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putExtra(Extras.TASK_ID, task.id);
            startActivity(intent);
            return;
        }
        if (detailPlaceholder != null) {
            detailPlaceholder.setVisibility(View.GONE);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.detailContainer, TaskDetailFragment.newInstance(task.id), "detail")
                .commit();
    }

    private boolean isShowing(String taskId) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("detail");
        return fragment instanceof TaskDetailFragment
                && taskId.equals(((TaskDetailFragment) fragment).taskId());
    }

    private void clearDetail() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("detail");
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        if (detailPlaceholder != null) {
            detailPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void openEditor() {
        startActivity(new Intent(this, TaskEditActivity.class));
    }

    private void confirmDelete(Task task) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_task_title)
                .setMessage(getString(R.string.delete_task_body, task.title))
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    store.remove(task.id);
                    load();
                    if (isShowing(task.id)) {
                        clearDetail();
                    }
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
    public void onTaskCompleted() {
        load();
        clearDetail();
    }

    @Override
    public void onTaskEdited() {
        load();
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
