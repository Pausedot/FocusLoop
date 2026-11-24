package itech3106.focusloop.planning;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.adapter.TimeSlotAdapter;
import itech3106.focusloop.adapter.WeekAdapter;
import itech3106.focusloop.data.Dates;
import itech3106.focusloop.data.FocusSession;
import itech3106.focusloop.data.PlanStore;
import itech3106.focusloop.data.SessionStore;
import itech3106.focusloop.data.Task;
import itech3106.focusloop.data.TaskStore;
import itech3106.focusloop.data.TimeSlot;
import itech3106.focusloop.data.WeekDay;
import itech3106.focusloop.tasks.TaskDetailActivity;
import itech3106.focusloop.tasks.TaskInboxActivity;

public class TimeBlocksActivity extends ModuleActivity {

    private final List<TimeSlot> slots = new ArrayList<>();
    private TimeSlotAdapter adapter;
    private WeekAdapter weekAdapter;
    private PlanStore planStore;
    private RecyclerView slotList;
    private RecyclerView weekList;
    private TextView weekSummary;
    private TextView planHint;
    private MaterialButton btnPrev;
    private MaterialButton btnNext;
    private ChipGroup viewToggle;
    private ActivityResultLauncher<Intent> taskPicker;
    private TextView dialogTaskValue;
    private String dialogTaskId;

    private boolean weekMode;
    private int dayOffset;
    private int weekOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_blocks);
        bindChrome(R.string.title_plan);

        taskPicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        return;
                    }
                    dialogTaskId = result.getData().getStringExtra(Extras.TASK_ID);
                    String title = result.getData().getStringExtra(Extras.TASK_TITLE);
                    if (dialogTaskValue != null) {
                        dialogTaskValue.setText(title == null
                                ? getString(R.string.dialog_no_task) : title);
                    }
                });

        planStore = new PlanStore(this);
        dayOffset = getIntent().getIntExtra(Extras.DAY_OFFSET, 0);
        if (savedInstanceState != null) {
            weekMode = savedInstanceState.getBoolean("weekMode", false);
            dayOffset = savedInstanceState.getInt("dayOffset", dayOffset);
            weekOffset = savedInstanceState.getInt("weekOffset", 0);
        }
        slots.addAll(planStore.forDay(Dates.today() + dayOffset));
        adapter = new TimeSlotAdapter(slots, currentHour(), this::showBlockDialog);
        slotList = findViewById(R.id.slotList);
        slotList.setLayoutManager(new LinearLayoutManager(this));
        slotList.setAdapter(adapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                int from = viewHolder.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
                    return false;
                }
                TimeSlot first = slots.get(from);
                TimeSlot second = slots.get(to);
                String title = first.title;
                String meta = first.meta;
                String taskId = first.taskId;
                first.title = second.title;
                first.meta = second.meta;
                first.taskId = second.taskId;
                second.title = title;
                second.meta = meta;
                second.taskId = taskId;
                planStore.save(first);
                planStore.save(second);
                adapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Dragging only; no swipe action on the hour list.
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        }).attachToRecyclerView(slotList);

        weekList = findViewById(R.id.weekList);
        weekList.setLayoutManager(new LinearLayoutManager(this));

        weekSummary = findViewById(R.id.weekSummary);
        planHint = findViewById(R.id.planHint);
        btnPrev = findViewById(R.id.btnPrevDay);
        btnNext = findViewById(R.id.btnNextDay);

        viewToggle = findViewById(R.id.viewToggle);
        viewToggle.check(weekMode ? R.id.chipWeek : R.id.chipDay);
        viewToggle.setOnCheckedStateChangeListener((group, checkedIds) -> {
            weekMode = checkedIds.contains(R.id.chipWeek);
            applyMode();
        });

        btnPrev.setOnClickListener(v -> shift(-1));
        btnNext.setOnClickListener(v -> shift(1));

        applyMode();
    }

    private void shift(int direction) {
        if (weekMode) {
            weekOffset += direction;
            refreshWeek();
        } else {
            dayOffset += direction;
            loadDay();
        }
        updateHeader();
    }

    private void loadDay() {
        slots.clear();
        slots.addAll(planStore.forDay(Dates.today() + dayOffset));
        adapter.notifyDataSetChanged();
    }

    private static String currentHour() {
        return String.format(Locale.ENGLISH, "%02d:00",
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    private void applyMode() {
        if (weekMode) {
            refreshWeek();
        }
        slotList.setVisibility(weekMode ? View.GONE : View.VISIBLE);
        weekList.setVisibility(weekMode ? View.VISIBLE : View.GONE);
        weekSummary.setVisibility(weekMode ? View.VISIBLE : View.GONE);
        planHint.setVisibility(View.VISIBLE);
        planHint.setText(weekMode ? R.string.plan_week_hint : R.string.plan_hint);

        btnPrev.setContentDescription(getString(
                weekMode ? R.string.cd_previous_week : R.string.cd_previous_day));
        btnNext.setContentDescription(getString(
                weekMode ? R.string.cd_next_week : R.string.cd_next_day));

        updateHeader();
    }

    private void updateHeader() {
        TextView dateText = findViewById(R.id.dateText);
        TextView dateCaption = findViewById(R.id.dateCaption);

        if (weekMode) {
            Calendar day = Calendar.getInstance();
            day.add(Calendar.DAY_OF_YEAR, weekOffset * 7);
            int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
            day.add(Calendar.DAY_OF_YEAR, -((dayOfWeek + 5) % 7));

            Calendar start = (Calendar) day.clone();
            day.add(Calendar.DAY_OF_YEAR, 6);

            SimpleDateFormat format = new SimpleDateFormat("d MMM", Locale.ENGLISH);
            dateText.setText(format.format(start.getTime()) + " – " + format.format(day.getTime()));

            int captionRes;
            if (weekOffset == 0) {
                captionRes = R.string.stats_this_week;
            } else if (weekOffset == -1) {
                captionRes = R.string.stats_last_week;
            } else if (weekOffset == 1) {
                captionRes = R.string.stats_next_week;
            } else {
                captionRes = R.string.stats_selected_week;
            }
            dateCaption.setText(captionRes);
            return;
        }

        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_YEAR, dayOffset);
        dateText.setText(new SimpleDateFormat("EEE d MMM", Locale.ENGLISH).format(day.getTime()));

        int captionRes;
        switch (dayOffset) {
            case 0:
                captionRes = R.string.plan_today;
                break;
            case 1:
                captionRes = R.string.plan_tomorrow;
                break;
            case -1:
                captionRes = R.string.plan_yesterday;
                break;
            default:
                captionRes = R.string.plan_selected;
                break;
        }
        dateCaption.setText(captionRes);
    }

    private void refreshWeek() {
        List<WeekDay> days = weekDays();
        weekAdapter = new WeekAdapter(days, this::openDay, this::openTask);
        weekList.setAdapter(weekAdapter);

        int totalMinutes = 0;
        for (WeekDay day : days) {
            totalMinutes += day.minutes;
        }

        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
        long start = Dates.startOfDay((int) monday.toEpochDay());
        long end = Dates.startOfDay((int) monday.plusDays(7).toEpochDay());
        int sessions = 0;
        for (FocusSession session : new SessionStore(this).all()) {
            if (session.startedAt >= start && session.startedAt < end) {
                sessions++;
            }
        }
        weekSummary.setText(getString(R.string.plan_week_summary_format,
                duration(totalMinutes), sessions));
    }

    private List<WeekDay> weekDays() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
        List<WeekDay> days = new ArrayList<>();
        PlanStore plan = new PlanStore(this);
        TaskStore tasks = new TaskStore(this);

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            int epochDay = (int) date.toEpochDay();

            List<Task> due = new ArrayList<>();
            boolean allDone = true;
            for (Task task : tasks.all()) {
                if (task.dueAt > 0 && Dates.epochDay(task.dueAt) == epochDay) {
                    due.add(task);
                    if (!task.done) {
                        allDone = false;
                    }
                }
            }
            if (due.isEmpty()) {
                allDone = false;
            }

            List<WeekDay.WeekTask> items = new ArrayList<>();
            for (Task task : due) {
                if (!task.done) {
                    items.add(new WeekDay.WeekTask(task.id, task.title));
                }
            }

            String label = date.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    .toUpperCase(Locale.ENGLISH);
            days.add(new WeekDay(label, String.valueOf(date.getDayOfMonth()),
                    plan.plannedMinutes(epochDay), Dates.isToday(epochDay),
                    allDone, items, epochDay));
        }
        return days;
    }

    private void openDay(WeekDay day) {
        if (day.epochDay == 0) {
            return;
        }
        dayOffset = day.epochDay - Dates.today();
        if (viewToggle.getCheckedChipId() != R.id.chipDay) {
            viewToggle.check(R.id.chipDay);
        } else {
            weekMode = false;
            applyMode();
        }
        loadDay();
        updateHeader();
    }

    private String duration(int minutes) {
        if (minutes < 60) {
            return getString(R.string.stats_duration_m, minutes);
        }
        return getString(R.string.stats_duration_hm, minutes / 60, minutes % 60);
    }

    private String endHour(String hour) {
        try {
            int value = Integer.parseInt(hour.substring(0, 2));
            return String.format(Locale.ENGLISH, "%02d:00", (value + 1) % 24);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return hour;
        }
    }

    private void openTask(WeekDay.WeekTask task) {        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(Extras.TASK_ID, task.id);
        intent.putExtra(Extras.TASK_TITLE, task.title);
        startActivity(intent);
    }

    private void showBlockDialog(TimeSlot slot) {
        View content = getLayoutInflater().inflate(R.layout.dialog_time_block, null);
        TextInputEditText edit = content.findViewById(R.id.blockEdit);
        TextInputLayout inputLayout = content.findViewById(R.id.blockInputLayout);
        boolean isNew = slot.title == null;
        edit.setText(slot.title);
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        dialogTaskId = slot.taskId;
        Task linkedTask = dialogTaskId == null ? null : new TaskStore(this).byId(dialogTaskId);
        dialogTaskValue = content.findViewById(R.id.blockTaskValue);
        dialogTaskValue.setText(linkedTask == null
                ? getString(R.string.dialog_no_task) : linkedTask.title);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(slot.title == null ? R.string.dialog_new_block : R.string.dialog_edit_block)
                .setView(content);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setOnDismissListener(d -> {
            dialogTaskValue = null;
            dialogTaskId = null;
        });

        View delete = content.findViewById(R.id.blockDelete);
        delete.setVisibility(slot.title == null ? View.GONE : View.VISIBLE);
        delete.setOnClickListener(v -> {
            planStore.clear(slot);
            adapter.notifyItemChanged(slots.indexOf(slot));
            dialog.dismiss();
        });
        content.findViewById(R.id.blockCancel).setOnClickListener(v -> dialog.dismiss());
        content.findViewById(R.id.blockSave).setOnClickListener(v -> {
            String text = edit.getText() == null ? "" : edit.getText().toString().trim();
            if (text.isEmpty()) {
                inputLayout.setError(getString(R.string.dialog_block_required));
                return;
            }
            slot.title = text;
            slot.taskId = dialogTaskId;
            if (isNew) {
                slot.meta = getString(R.string.block_meta_format, slot.hour, endHour(slot.hour));
            }
            planStore.save(slot);
            adapter.notifyItemChanged(slots.indexOf(slot));
            dialog.dismiss();
        });
        content.findViewById(R.id.blockTaskCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskInboxActivity.class);
            intent.putExtra(Extras.PICK_TASK, true);
            taskPicker.launch(intent);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("weekMode", weekMode);
        outState.putInt("dayOffset", dayOffset);
        outState.putInt("weekOffset", weekOffset);
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_plan;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_time_blocks;
    }
}
