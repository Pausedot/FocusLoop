package itech3106.focusloop.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import itech3106.focusloop.Extras;
import itech3106.focusloop.R;
import itech3106.focusloop.data.SessionStore;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.data.TaskStore;
import itech3106.focusloop.focus.PomodoroActivity;

/**
 * The task detail body. The same fragment is hosted by TaskDetailActivity on a phone and
 * by the two-pane inbox on a tablet.
 */
public class TaskDetailFragment extends Fragment {

    public interface Listener {
        void onTaskCompleted();

        void onTaskEdited();
    }

    private static final String ARG_TASK_ID = "task_id";

    private TaskStore store;
    private Task task;
    private Listener listener;

    public static TaskDetailFragment newInstance(String taskId) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_ID, taskId);
        fragment.setArguments(args);
        return fragment;
    }

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
        return inflater.inflate(R.layout.fragment_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        store = new TaskStore(requireContext());
        bind();
    }

    @Override
    public void onResume() {
        super.onResume();
        bind();
    }

    public void reload() {
        bind();
    }

    public String taskId() {
        return getArguments() == null ? null : getArguments().getString(ARG_TASK_ID);
    }

    private void bind() {
        View view = getView();
        if (view == null) {
            return;
        }
        task = store.byId(taskId());
        if (task == null) {
            ((TextView) view.findViewById(R.id.detailTitle)).setText(R.string.dialog_no_task);
            view.findViewById(R.id.btnStartFocus).setEnabled(false);
            view.findViewById(R.id.btnComplete).setEnabled(false);
            view.findViewById(R.id.btnEdit).setEnabled(false);
            return;
        }

        ((TextView) view.findViewById(R.id.detailTitle)).setText(task.title);

        TextView badge = view.findViewById(R.id.detailBadge);
        badge.setText(String.valueOf(task.priority));
        badge.setBackgroundResource(task.priority == 'A'
                ? R.drawable.bg_badge_a
                : R.drawable.bg_badge_outline);

        ((TextView) view.findViewById(R.id.detailQuadrant)).setText(
                task.quadrant + " · " + getString(quadrantLabel(task.quadrant)));
        ((TextView) view.findViewById(R.id.detailDue)).setText(task.due);
        ((TextView) view.findViewById(R.id.detailNotes)).setText(task.notes);

        SessionStore sessions = new SessionStore(requireContext());
        int count = sessions.countForTask(task.id);
        int minutes = sessions.minutesForTask(task.id);
        ((TextView) view.findViewById(R.id.detailFocusValue)).setText(
                getResources().getQuantityString(R.plurals.detail_sessions, count, count)
                        + " · " + minutes + " min");

        view.findViewById(R.id.btnEdit).setOnClickListener(v -> openEditor());
        view.findViewById(R.id.btnStartFocus).setOnClickListener(v -> startFocus());
        view.findViewById(R.id.btnComplete).setOnClickListener(v -> complete());
    }

    private void openEditor() {
        if (task == null) {
            return;
        }
        Intent intent = new Intent(requireContext(), TaskEditActivity.class);
        intent.putExtra(Extras.TASK_ID, task.id);
        startActivity(intent);
        if (listener != null) {
            listener.onTaskEdited();
        }
    }

    private void startFocus() {
        if (task == null) {
            return;
        }
        Intent intent = new Intent(requireContext(), PomodoroActivity.class);
        intent.putExtra(Extras.TASK_ID, task.id);
        intent.putExtra(Extras.TASK_TITLE, task.title);
        startActivity(intent);
    }

    private void complete() {
        if (task == null) {
            return;
        }
        store.setDone(task.id, true);
        if (listener != null) {
            listener.onTaskCompleted();
        } else if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private static int quadrantLabel(String quadrant) {
        switch (quadrant) {
            case "Q2":
                return R.string.matrix_q2;
            case "Q3":
                return R.string.matrix_q3;
            case "Q4":
                return R.string.matrix_q4;
            default:
                return R.string.matrix_q1;
        }
    }
}
