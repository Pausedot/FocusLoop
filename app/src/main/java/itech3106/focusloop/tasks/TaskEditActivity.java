package itech3106.focusloop.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import itech3106.focusloop.BaseActivity;
import itech3106.focusloop.Extras;
import itech3106.focusloop.R;
import itech3106.focusloop.data.Dates;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.data.TaskStore;

public class TaskEditActivity extends BaseActivity {

    private RadioGroup priorityGroup;
    private TextView priorityHint;
    private TaskStore store;
    private Task existing;
    private long pickedDueAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);

        store = new TaskStore(this);
        String taskId = getIntent().getStringExtra(Extras.TASK_ID);
        existing = taskId == null ? null : store.byId(taskId);
        bindChrome(existing == null ? R.string.title_new_task : R.string.title_edit_task);

        priorityGroup = findViewById(R.id.priorityGroup);
        priorityHint = findViewById(R.id.priorityHint);

        if (existing != null) {
            fillForm(existing);
        } else {
            priorityHint.setText(R.string.priority_hint_a);
        }

        priorityGroup.setOnCheckedChangeListener((group, checkedId) ->
                priorityHint.setText(hintFor(checkedId)));

        findViewById(R.id.dueCard).setOnClickListener(v -> pickDate());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void fillForm(Task task) {
        ((TextInputEditText) findViewById(R.id.titleEdit)).setText(task.title);
        ((TextInputEditText) findViewById(R.id.notesEdit)).setText(task.notes);
        ((TextView) findViewById(R.id.dueValue)).setText(task.due);
        pickedDueAt = task.dueAt;
        priorityGroup.check(radioFor(task.priority));
        priorityHint.setText(hintFor(radioFor(task.priority)));
    }

    private void pickDate() {
        Calendar calendar = Calendar.getInstance();
        if (pickedDueAt > 0) {
            calendar.setTime(new Date(pickedDueAt));
        }
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day);
            ((TextView) findViewById(R.id.dueValue)).setText(
                    new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
                            .format(picked.getTime()));
            ((TextView) findViewById(R.id.dueHint)).setText(R.string.label_due);
            pickedDueAt = Dates.at((int) picked.toInstant().atZone(Dates.zone())
                    .toLocalDate().toEpochDay(), 18, 0);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        TextInputEditText titleEdit = findViewById(R.id.titleEdit);
        String title = titleEdit.getText() == null ? "" : titleEdit.getText().toString().trim();
        if (title.isEmpty()) {
            TextInputLayout wrapper = findViewById(R.id.titleInput);
            wrapper.setError(getString(R.string.error_title_required));
            titleEdit.requestFocus();
            return;
        }

        TextInputEditText notesEdit = findViewById(R.id.notesEdit);
        String notes = notesEdit.getText() == null ? "" : notesEdit.getText().toString().trim();
        char priority = priorityFor(priorityGroup.getCheckedRadioButtonId());
        String quadrant = quadrantFor(priority);
        String dueLabel = ((TextView) findViewById(R.id.dueValue)).getText().toString();

        if (existing == null) {
            Task task = new Task("t" + System.currentTimeMillis(), title, priority,
                    dueLabel, quadrant, notes, false);
            task.dueAt = pickedDueAt;
            store.add(task);
        } else {
            Task updated = new Task(existing.id, title, priority, dueLabel, quadrant,
                    notes, existing.done);
            updated.dueAt = pickedDueAt;
            updated.completedAt = existing.completedAt;
            updated.frog = existing.frog;
            updated.attachmentPath = existing.attachmentPath;
            store.upsert(updated);
        }
        finish();
    }

    private char priorityFor(int checkedId) {
        if (checkedId == R.id.priorityB) {
            return 'B';
        }
        if (checkedId == R.id.priorityC) {
            return 'C';
        }
        if (checkedId == R.id.priorityD) {
            return 'D';
        }
        if (checkedId == R.id.priorityE) {
            return 'E';
        }
        return 'A';
    }

    private String quadrantFor(char priority) {
        switch (priority) {
            case 'A':
                return "Q1";
            case 'B':
                return "Q2";
            case 'C':
                return "Q3";
            default:
                return "Q4";
        }
    }

    private int radioFor(char priority) {
        switch (priority) {
            case 'B':
                return R.id.priorityB;
            case 'C':
                return R.id.priorityC;
            case 'D':
                return R.id.priorityD;
            case 'E':
                return R.id.priorityE;
            default:
                return R.id.priorityA;
        }
    }

    private int hintFor(int checkedId) {
        if (checkedId == R.id.priorityB) {
            return R.string.priority_hint_b;
        }
        if (checkedId == R.id.priorityC) {
            return R.string.priority_hint_c;
        }
        if (checkedId == R.id.priorityD) {
            return R.string.priority_hint_d;
        }
        if (checkedId == R.id.priorityE) {
            return R.string.priority_hint_e;
        }
        return R.string.priority_hint_a;
    }
}
