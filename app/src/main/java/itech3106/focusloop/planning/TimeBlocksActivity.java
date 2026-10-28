package itech3106.focusloop.planning;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.adapter.TimeSlotAdapter;
import itech3106.focusloop.adapter.WeekAdapter;
import itech3106.focusloop.data.DemoData;
import itech3106.focusloop.data.TimeSlot;
import itech3106.focusloop.data.WeekDay;
import itech3106.focusloop.tasks.TaskDetailActivity;
import itech3106.focusloop.tasks.TaskInboxActivity;

public class TimeBlocksActivity extends ModuleActivity {

    private final List<TimeSlot> slots = new ArrayList<>();
    private TimeSlotAdapter adapter;
    private RecyclerView slotList;
    private RecyclerView weekList;
    private TextView weekSummary;
    private TextView planHint;
    private MaterialButton btnPrev;
    private MaterialButton btnNext;

    private boolean weekMode;
    private int dayOffset;
    private int weekOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_blocks);
        bindChrome(R.string.title_plan);

        slots.addAll(DemoData.slots());
        adapter = new TimeSlotAdapter(slots, this::showBlockDialog);
        slotList = findViewById(R.id.slotList);
        slotList.setLayoutManager(new LinearLayoutManager(this));
        slotList.setAdapter(adapter);

        WeekAdapter weekAdapter = new WeekAdapter(DemoData.weekDays(), this::openTask);
        weekList = findViewById(R.id.weekList);
        weekList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        weekList.setAdapter(weekAdapter);

        weekSummary = findViewById(R.id.weekSummary);
        planHint = findViewById(R.id.planHint);
        btnPrev = findViewById(R.id.btnPrevDay);
        btnNext = findViewById(R.id.btnNextDay);

        ChipGroup viewToggle = findViewById(R.id.viewToggle);
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
        } else {
            dayOffset += direction;
        }
        updateHeader();
    }

    private void applyMode() {
        slotList.setVisibility(weekMode ? View.GONE : View.VISIBLE);
        weekList.setVisibility(weekMode ? View.VISIBLE : View.GONE);
        weekSummary.setVisibility(weekMode ? View.VISIBLE : View.GONE);
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

    private void openTask(WeekDay.WeekTask task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra(Extras.TASK_ID, task.id);
        intent.putExtra(Extras.TASK_TITLE, task.title);
        startActivity(intent);
    }

    private void showBlockDialog(TimeSlot slot) {
        View content = getLayoutInflater().inflate(R.layout.dialog_time_block, null);
        TextInputEditText edit = content.findViewById(R.id.blockEdit);
        edit.setText(slot.title);
        if (slot.title != null) {
            ((TextView) content.findViewById(R.id.blockTaskValue)).setText(slot.title);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(slot.title == null ? R.string.dialog_new_block : R.string.dialog_edit_block)
                .setView(content)
                .setPositiveButton(R.string.action_save_block, (dialog, which) -> {
                    String text = edit.getText() == null ? "" : edit.getText().toString().trim();
                    if (text.isEmpty()) {
                        return;
                    }
                    slot.title = text;
                    slot.meta = getString(R.string.block_meta_format, slot.hour);
                    adapter.notifyItemChanged(slots.indexOf(slot));
                })
                .setNegativeButton(R.string.action_cancel, null);

        if (slot.title != null) {
            builder.setNeutralButton(R.string.action_delete_block, (dialog, which) -> {
                slot.title = null;
                slot.meta = null;
                adapter.notifyItemChanged(slots.indexOf(slot));
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();
        content.findViewById(R.id.blockTaskCard).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, TaskInboxActivity.class));
        });
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
