package itech3106.focusloop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import itech3106.focusloop.R;
import itech3106.focusloop.data.TimeSlot;

/** The hour timeline. Empty hours show an add affordance, 10:00 is marked as now for the demo. */
public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.Holder> {

    public interface OnSlotClick {
        void onSlotClick(TimeSlot slot);
    }

    private static final String NOW_HOUR = "10:00";

    private final List<TimeSlot> slots;
    private final OnSlotClick listener;

    public TimeSlotAdapter(List<TimeSlot> slots, OnSlotClick listener) {
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        TimeSlot slot = slots.get(position);
        boolean isNow = NOW_HOUR.equals(slot.hour);
        boolean filled = slot.title != null;

        holder.hour.setText(slot.hour);
        holder.nowTag.setVisibility(isNow ? View.VISIBLE : View.GONE);

        holder.block.setVisibility(filled ? View.VISIBLE : View.GONE);
        holder.add.setVisibility(filled ? View.GONE : View.VISIBLE);
        if (filled) {
            holder.blockTitle.setText(slot.title);
            holder.blockMeta.setText(slot.meta);
            holder.block.setBackgroundResource(isNow ? R.drawable.bg_hour_now : R.drawable.bg_block);
        }

        int railColor = ContextCompat.getColor(holder.itemView.getContext(),
                isNow ? R.color.accent : R.color.hairline);
        holder.rail.setBackgroundColor(railColor);
        holder.dot.setBackgroundResource(isNow ? R.drawable.bg_dot_accent : R.drawable.bg_dot_muted);

        holder.itemView.setOnClickListener(v -> listener.onSlotClick(slot));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView hour;
        final View rail;
        final View dot;
        final View block;
        final TextView blockTitle;
        final TextView blockMeta;
        final TextView add;
        final TextView nowTag;

        Holder(@NonNull View itemView) {
            super(itemView);
            hour = itemView.findViewById(R.id.hourText);
            rail = itemView.findViewById(R.id.rail);
            dot = itemView.findViewById(R.id.dot);
            block = itemView.findViewById(R.id.slotBlock);
            blockTitle = itemView.findViewById(R.id.slotBlockTitle);
            blockMeta = itemView.findViewById(R.id.slotBlockMeta);
            add = itemView.findViewById(R.id.slotAdd);
            nowTag = itemView.findViewById(R.id.nowTag);
        }
    }
}
