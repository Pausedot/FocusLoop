package itech3106.focusloop.focus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import itech3106.focusloop.Extras;
import itech3106.focusloop.R;
import itech3106.focusloop.data.FocusSession;
import itech3106.focusloop.data.SessionStore;
import itech3106.focusloop.tasks.TaskDetailActivity;

/** Read-only detail for one finished or stopped session. */
public class SessionDetailFragment extends Fragment {

    private static final String ARG_SESSION_ID = "session_id";

    public static SessionDetailFragment newInstance(String sessionId) {
        SessionDetailFragment fragment = new SessionDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SESSION_ID, sessionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_session_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String id = getArguments() == null ? null : getArguments().getString(ARG_SESSION_ID);
        FocusSession session = id == null ? null
                : new SessionStore(requireContext()).byId(id);

        if (session == null) {
            ((TextView) view.findViewById(R.id.detailSessionTask)).setText(R.string.dialog_no_task);
            view.findViewById(R.id.btnStartAnother).setEnabled(false);
            view.findViewById(R.id.btnOpenTask).setVisibility(View.GONE);
            return;
        }

        ((TextView) view.findViewById(R.id.detailSessionTask)).setText(session.title);
        ((TextView) view.findViewById(R.id.detailSessionWhen))
                .setText(session.group + " · " + session.time);
        ((TextView) view.findViewById(R.id.detailSessionDuration))
                .setText(session.minutes + " min");

        TextView status = view.findViewById(R.id.detailSessionStatus);
        status.setText(session.completed ? R.string.history_completed : R.string.history_stopped);
        status.setTextColor(ContextCompat.getColor(requireContext(),
                session.completed ? R.color.ink_muted : R.color.danger));

        ((TextView) view.findViewById(R.id.detailSessionRound))
                .setText(getString(R.string.history_round_format, session.round));

        View openTask = view.findViewById(R.id.btnOpenTask);
        if (session.taskId == null) {
            openTask.setVisibility(View.GONE);
        } else {
            openTask.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), TaskDetailActivity.class);
                intent.putExtra(Extras.TASK_ID, session.taskId);
                startActivity(intent);
            });
        }

        view.findViewById(R.id.btnStartAnother).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FiveSecondActivity.class);
            intent.putExtra(Extras.TASK_ID, session.taskId);
            intent.putExtra(Extras.TASK_TITLE, session.taskId == null ? null : session.title);
            startActivity(intent);
        });
    }
}
