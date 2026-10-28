package itech3106.focusloop.adapter;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import itech3106.focusloop.R;
import itech3106.focusloop.data.WeekDay;

/** A horizontal row of day cards for the Week mode on the Plan screen. */
public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.Holder> {

    public interface OnTaskClick {
        void onTaskClick(WeekDay.WeekTask task);
    }

    private final List<WeekDay> days;
    private final OnTaskClick listener;

    public WeekAdapter(List<WeekDay> days, OnTaskClick listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_day, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        WeekDay day = days.get(position);
        holder.dayLabel.setText(day.label);
        holder.dateNumber.setText(day.date);
        holder.minutes.setText(day.minutes + " min");
        holder.doneCheck.setVisibility(day.allDone ? View.VISIBLE : View.GONE);

        if (day.today) {
            holder.dateNumber.setBackgroundResource(R.drawable.bg_circle_accent);
            holder.dateNumber.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), R.color.on_accent));
        } else {
            holder.dateNumber.setBackgroundResource(0);
            holder.dateNumber.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), R.color.ink));
        }

        holder.taskContainer.removeAllViews();
        if (day.tasks.isEmpty()) {
            holder.taskContainer.addView(freeLabel(holder.itemView));
            return;
        }
        for (WeekDay.WeekTask task : day.tasks) {
            holder.taskContainer.addView(taskLabel(holder.itemView, task));
        }
    }

    private TextView freeLabel(View parent) {
        TextView text = new TextView(parent.getContext());
        text.setText(R.string.plan_week_free);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        text.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.ink_muted));
        return text;
    }

    private TextView taskLabel(View parent, WeekDay.WeekTask task) {
        TextView text = new TextView(parent.getContext());
        text.setText(task.title);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        text.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.ink));
        text.setMaxLines(2);
        text.setEllipsize(TextUtils.TruncateAt.END);
        text.setPadding(0, 0, 0, dp(parent, 6));
        text.setGravity(Gravity.START);

        TypedValue ripple = new TypedValue();
        parent.getContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, ripple, true);
        text.setBackgroundResource(ripple.resourceId);
        text.setOnClickListener(v -> listener.onTaskClick(task));
        return text;
    }

    private int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView dayLabel;
        final TextView dateNumber;
        final TextView minutes;
        final LinearLayout taskContainer;
        final ImageView doneCheck;

        Holder(@NonNull View itemView) {
            super(itemView);
            dayLabel = itemView.findViewById(R.id.dayLabel);
            dateNumber = itemView.findViewById(R.id.dateNumber);
            minutes = itemView.findViewById(R.id.minutes);
            taskContainer = itemView.findViewById(R.id.taskContainer);
            doneCheck = itemView.findViewById(R.id.doneCheck);
        }
    }
}
