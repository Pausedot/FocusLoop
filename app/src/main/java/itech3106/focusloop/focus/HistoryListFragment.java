package itech3106.focusloop.focus;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import itech3106.focusloop.R;
import itech3106.focusloop.adapter.SessionAdapter;
import itech3106.focusloop.data.FocusSession;
import itech3106.focusloop.data.SessionStore;

/** The totals and the grouped session list. */
public class HistoryListFragment extends Fragment {

    public interface Listener {
        void onSessionSelected(FocusSession session);
    }

    private SessionStore store;
    private Listener listener;
    private RecyclerView list;
    private View emptyState;
    private TextView sessionsValue;
    private TextView minutesValue;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        store = new SessionStore(requireContext());
        list = view.findViewById(R.id.sessionList);
        emptyState = view.findViewById(R.id.emptyState);
        sessionsValue = view.findViewById(R.id.historySessionsValue);
        minutesValue = view.findViewById(R.id.historyMinutesValue);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        if (store == null || list == null) {
            return;
        }
        List<FocusSession> sessions = store.recent();
        List<Object> rows = groupRows(sessions);
        list.setAdapter(new SessionAdapter(rows, session -> {
            if (listener != null) {
                listener.onSessionSelected(session);
            }
        }));

        sessionsValue.setText(String.valueOf(store.today().size()));
        minutesValue.setText(String.valueOf(store.minutesToday()));

        boolean isEmpty = rows.isEmpty();
        list.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
}
