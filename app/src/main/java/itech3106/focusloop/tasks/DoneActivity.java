package itech3106.focusloop.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.adapter.DoneAdapter;
import itech3106.focusloop.data.Dates;
import itech3106.focusloop.data.SessionStore;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.data.TaskStore;

public class DoneActivity extends ModuleActivity {

    private final List<Object> rows = new ArrayList<>();
    private TaskStore store;
    private SessionStore sessions;
    private DoneAdapter adapter;
    private RecyclerView list;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);
        bindChrome(R.string.title_done);

        store = new TaskStore(this);
        sessions = new SessionStore(this);

        list = findViewById(R.id.doneList);
        emptyState = findViewById(R.id.emptyState);
        adapter = new DoneAdapter(rows, new DoneAdapter.Listener() {
            @Override
            public void onOpen(DoneAdapter.DoneRow row) {
                Intent intent = new Intent(DoneActivity.this, TaskDetailActivity.class);
                intent.putExtra(Extras.TASK_ID, row.task.id);
                startActivity(intent);
            }

            @Override
            public void onRestore(DoneAdapter.DoneRow row) {
                store.setDone(row.task.id, false);
                load();
            }
        });
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            load();
        }
    }

    private void load() {
        rows.clear();
        int count = 0;
        int minutes = 0;
        int group = 0;

        for (Task task : store.done()) {
            int day = task.completedAt == 0 ? 0 : Dates.epochDay(task.completedAt);
            if (day != group) {
                group = day;
                rows.add(headerFor(day));
            }
            int taskMinutes = sessions.minutesForTask(task.id);
            rows.add(new DoneAdapter.DoneRow(task, taskMinutes));
            count++;
            minutes += taskMinutes;
        }

        ((TextView) findViewById(R.id.doneSummary))
                .setText(getString(R.string.done_summary_format, count, minutes));
        adapter.notifyDataSetChanged();

        boolean isEmpty = count == 0;
        list.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private String headerFor(int epochDay) {
        int today = Dates.today();
        if (epochDay == today) {
            return getString(R.string.done_today);
        }
        if (epochDay == today - 1) {
            return getString(R.string.done_yesterday);
        }
        return Dates.dayLabel(epochDay);
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_tasks;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_done;
    }
}
