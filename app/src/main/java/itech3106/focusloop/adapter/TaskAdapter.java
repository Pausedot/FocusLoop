package itech3106.focusloop.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

import itech3106.focusloop.R;
import itech3106.focusloop.data.DemoData;
import itech3106.focusloop.data.Task;

/** Rows are either a section header (String) or a task. */
public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnTaskClick {
        void onTaskClick(Task task);

        void onTaskLongClick(Task task);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;

    private final List<Object> rows;
    private final OnTaskClick listener;

    public TaskAdapter(List<Object> rows, OnTaskClick listener) {
        this.rows = rows;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof Task ? TYPE_TASK : TYPE_HEADER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new TaskHolder(inflater.inflate(R.layout.item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).label.setText((String) row);
            return;
        }

        TaskHolder taskHolder = (TaskHolder) holder;
        Task task = (Task) row;
        taskHolder.title.setText(task.title);
        taskHolder.meta.setText(task.due + " · " + task.quadrant);
        taskHolder.badge.setText(String.valueOf(task.priority));
        taskHolder.check.setChecked(task.done);
        applyBadge(taskHolder, task.priority);
        applyDone(taskHolder.title, task.done);

        taskHolder.check.setOnClickListener(v -> {
            int pos = taskHolder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            task.done = taskHolder.check.isChecked();
            DemoData.setDone(task.id, task.done);
            notifyItemChanged(pos);
        });
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onTaskLongClick(task);
            return true;
        });
    }

    private void applyBadge(TaskHolder holder, char priority) {
        int textColor;
        switch (priority) {
            case 'A':
                holder.badge.setBackgroundResource(R.drawable.bg_badge_a);
                textColor = R.color.accent;
                break;
            case 'B':
            case 'C':
                holder.badge.setBackgroundResource(R.drawable.bg_badge_outline);
                textColor = R.color.ink_muted;
                break;
            default:
                holder.badge.setBackgroundResource(R.drawable.bg_badge_muted);
                textColor = R.color.ink_muted;
                break;
        }
        holder.badge.setTextColor(ContextCompat.getColor(holder.badge.getContext(), textColor));
    }

    static void applyDone(TextView title, boolean done) {
        int flags = done
                ? title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG;
        title.setPaintFlags(flags);
        title.setAlpha(done ? 0.45f : 1f);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        final TextView label;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.headerLabel);
        }
    }

    static class TaskHolder extends RecyclerView.ViewHolder {
        final MaterialCheckBox check;
        final TextView title;
        final TextView meta;
        final TextView badge;

        TaskHolder(@NonNull View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.taskCheck);
            title = itemView.findViewById(R.id.taskTitle);
            meta = itemView.findViewById(R.id.taskMeta);
            badge = itemView.findViewById(R.id.taskBadge);
        }
    }
}
