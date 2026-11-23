package itech3106.focusloop.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    private ActivityResultLauncher<String[]> photoPicker;
    private ImageView photoView;
    private View photoCard;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), this::onPhotoPicked);
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

        photoCard = view.findViewById(R.id.photoCard);
        photoView = view.findViewById(R.id.detailPhoto);
        view.findViewById(R.id.btnAddPhoto).setOnClickListener(v ->
                photoPicker.launch(new String[]{"image/*"}));
        view.findViewById(R.id.btnRemovePhoto).setOnClickListener(v -> removePhoto());
        bindPhoto();

        view.findViewById(R.id.btnEdit).setOnClickListener(v -> openEditor());
        view.findViewById(R.id.btnStartFocus).setOnClickListener(v -> startFocus());
        view.findViewById(R.id.btnComplete).setOnClickListener(v -> complete());
    }

    private void bindPhoto() {
        if (photoCard == null || photoView == null) {
            return;
        }
        File file = task == null || task.attachmentPath == null
                ? null : new File(task.attachmentPath);
        if (file != null && file.exists()) {
            photoCard.setVisibility(View.VISIBLE);
            photoView.setImageBitmap(decode(file));
        } else {
            photoCard.setVisibility(View.GONE);
            photoView.setImageDrawable(null);
        }
    }

    private void onPhotoPicked(Uri uri) {
        if (uri == null || task == null) {
            return;
        }
        File dir = new File(requireContext().getFilesDir(), "attachments");
        if (!dir.exists() && !dir.mkdirs()) {
            return;
        }
        File dest = new File(dir, task.id + "-" + System.currentTimeMillis() + ".jpg");
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(dest)) {
            if (in == null) {
                return;
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
        } catch (IOException ignored) {
            return;
        }

        if (task.attachmentPath != null) {
            new File(task.attachmentPath).delete();
        }
        task.attachmentPath = dest.getAbsolutePath();
        store.upsert(task);
        bindPhoto();
        if (listener != null) {
            listener.onTaskEdited();
        }
    }

    private void removePhoto() {
        if (task == null) {
            return;
        }
        if (task.attachmentPath != null) {
            new File(task.attachmentPath).delete();
        }
        task.attachmentPath = null;
        store.upsert(task);
        bindPhoto();
        if (listener != null) {
            listener.onTaskEdited();
        }
    }

    private static Bitmap decode(File file) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        int sample = 1;
        while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) {
            sample *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    private void openEditor() {        if (task == null) {
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
