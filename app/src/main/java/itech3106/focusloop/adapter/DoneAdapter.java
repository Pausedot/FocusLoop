package itech3106.focusloop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import itech3106.focusloop.R;
import itech3106.focusloop.data.Task;

/** Date header, then the tasks finished that day. */
public class DoneAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onOpen(DoneRow row);

        void onRestore(DoneRow row);
    }

    public static class DoneRow {
        public final Task task;
        public final int minutes;

        public DoneRow(Task task, int minutes) {
            this.task = task;
            this.minutes = minutes;
        }
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ROW = 1;

    private final List<Object> rows;
    private final Listener listener;

    public DoneAdapter(List<Object> rows, Listener listener) {
        this.rows = rows;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof DoneRow ? TYPE_ROW : TYPE_HEADER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_session_group, parent, false));
        }
        return new RowHolder(inflater.inflate(R.layout.item_done, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).label.setText((String) row);
            return;
        }

        DoneRow done = (DoneRow) row;
        RowHolder rowHolder = (RowHolder) holder;
        rowHolder.title.setText(done.task.title);
        rowHolder.meta.setText(done.task.priority + " · " + done.task.quadrant + " · "
                + rowHolder.itemView.getContext().getString(
                R.string.done_focus_format, done.minutes));
        holder.itemView.setOnClickListener(v -> listener.onOpen(done));
        rowHolder.restore.setOnClickListener(v -> listener.onRestore(done));
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

    static class RowHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView meta;
        final View restore;

        RowHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.doneTitle);
            meta = itemView.findViewById(R.id.doneMeta);
            restore = itemView.findViewById(R.id.doneRestore);
        }
    }
}
