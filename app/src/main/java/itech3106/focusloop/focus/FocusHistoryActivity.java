package itech3106.focusloop.focus;

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
import itech3106.focusloop.adapter.SessionAdapter;
import itech3106.focusloop.data.DemoData;
import itech3106.focusloop.data.FocusSession;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.tasks.TaskDetailActivity;

public class FocusHistoryActivity extends ModuleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_history);
        bindChrome(R.string.title_history);

        List<Object> rows = DemoData.sessionRows();
        SessionAdapter adapter = new SessionAdapter(rows, this::openTask);
        RecyclerView list = findViewById(R.id.sessionList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        int sessions = 0;
        int minutes = 0;
        for (FocusSession session : DemoData.sessions()) {
            if ("26 OCT".equals(session.group)) {
                sessions++;
                minutes += session.minutes;
            }
        }
        ((TextView) findViewById(R.id.historySessionsValue)).setText(String.valueOf(sessions));
        ((TextView) findViewById(R.id.historyMinutesValue)).setText(String.valueOf(minutes));

        boolean isEmpty = rows.isEmpty();
        list.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        findViewById(R.id.emptyState).setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void openTask(FocusSession session) {
        Task task = DemoData.taskByTitle(session.title);
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(Extras.TASK_ID, task.id);
        intent.putExtra(Extras.TASK_TITLE, task.title);
        startActivity(intent);
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_focus;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_history;
    }
}
