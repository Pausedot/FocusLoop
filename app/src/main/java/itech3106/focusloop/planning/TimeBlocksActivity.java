package itech3106.focusloop.planning;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.adapter.TimeSlotAdapter;
import itech3106.focusloop.data.DemoData;
import itech3106.focusloop.data.TimeSlot;
import itech3106.focusloop.tasks.TaskInboxActivity;

public class TimeBlocksActivity extends ModuleActivity {

    private final List<TimeSlot> slots = new ArrayList<>();
    private TimeSlotAdapter adapter;
    private int dayOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_blocks);
        bindChrome(R.string.title_plan);

        slots.addAll(DemoData.slots());
        adapter = new TimeSlotAdapter(slots, this::showBlockDialog);
        RecyclerView list = findViewById(R.id.slotList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.btnPrevDay).setOnClickListener(v -> {
            dayOffset--;
            updateDate();
        });
        findViewById(R.id.btnNextDay).setOnClickListener(v -> {
            dayOffset++;
            updateDate();
        });
        updateDate();
    }

    private void updateDate() {
        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_YEAR, dayOffset);
        ((TextView) findViewById(R.id.dateText)).setText(
                new SimpleDateFormat("EEE d MMM", Locale.ENGLISH).format(day.getTime()));

        int caption;
        switch (dayOffset) {
            case 0:
                caption = R.string.plan_today;
                break;
            case 1:
                caption = R.string.plan_tomorrow;
                break;
            case -1:
                caption = R.string.plan_yesterday;
                break;
            default:
                caption = R.string.plan_selected;
                break;
        }
        ((TextView) findViewById(R.id.dateCaption)).setText(caption);
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
