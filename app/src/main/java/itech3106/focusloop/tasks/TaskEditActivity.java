package itech3106.focusloop.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import itech3106.focusloop.BaseActivity;
import itech3106.focusloop.Extras;
import itech3106.focusloop.R;
import itech3106.focusloop.data.DemoData;
import itech3106.focusloop.data.Task;

public class TaskEditActivity extends BaseActivity {

    private RadioGroup priorityGroup;
    private TextView priorityHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);

        String taskId = getIntent().getStringExtra(Extras.TASK_ID);
        boolean editing = taskId != null && !taskId.isEmpty();
        bindChrome(editing ? R.string.title_edit_task : R.string.title_new_task);

        priorityGroup = findViewById(R.id.priorityGroup);
        priorityHint = findViewById(R.id.priorityHint);

        if (editing) {
            fillForm(DemoData.task(taskId));
        } else {
            priorityHint.setText(R.string.priority_hint_a);
        }

        priorityGroup.setOnCheckedChangeListener((group, checkedId) -> priorityHint.setText(
                hintFor(checkedId)));

        findViewById(R.id.dueCard).setOnClickListener(v -> pickDate());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
    }

    private void fillForm(Task task) {
        ((TextInputEditText) findViewById(R.id.titleEdit)).setText(task.title);
        ((TextInputEditText) findViewById(R.id.notesEdit)).setText(task.notes);
        ((TextView) findViewById(R.id.dueValue)).setText(task.due);
        priorityGroup.check(radioFor(task.priority));
        priorityHint.setText(hintFor(radioFor(task.priority)));
    }

    private void pickDate() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day);
            TextView dueValue = findViewById(R.id.dueValue);
            dueValue.setText(new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
                    .format(picked.getTime()));
            TextView dueHint = findViewById(R.id.dueHint);
            dueHint.setText(R.string.label_due);
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
        finish();
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
