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
import itech3106.focusloop.data.FocusSession;
import itech3106.focusloop.data.SessionStore;
import itech3106.focusloop.tasks.TaskDetailActivity;

public class FocusHistoryActivity extends ModuleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_history);
        bindChrome(R.string.title_history);

        SessionStore store = new SessionStore(this);
        List<Object> rows = groupRows(store.recent());
        SessionAdapter adapter = new SessionAdapter(rows, this::openSession);
        RecyclerView list = findViewById(R.id.sessionList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        ((TextView) findViewById(R.id.historySessionsValue))
                .setText(String.valueOf(store.today().size()));
        ((TextView) findViewById(R.id.historyMinutesValue))
                .setText(String.valueOf(store.minutesToday()));

        boolean isEmpty = rows.isEmpty();
        list.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        findViewById(R.id.emptyState).setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private static List<Object> groupRows(List<FocusSession> sessions) {
        List<Object> rows = new ArrayList<>();
        String group = null;
        for (FocusSession session : sessions) {
            if (!session.group.equals(group)) {
                group = session.group;
                rows.add(group);
            }
            rows.add(session);
        }
        return rows;
    }

    private void openSession(FocusSession session) {
        if (session.taskId == null) {
            return;
        }
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(Extras.TASK_ID, session.taskId);
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
