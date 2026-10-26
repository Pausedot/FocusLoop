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
import itech3106.focusloop.data.FocusSession;

/** Date header, then the sessions of that day. */
public class SessionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnSessionClick {
        void onSessionClick(FocusSession session);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_SESSION = 1;

    private final List<Object> rows;
    private final OnSessionClick listener;

    public SessionAdapter(List<Object> rows, OnSessionClick listener) {
        this.rows = rows;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof FocusSession ? TYPE_SESSION : TYPE_HEADER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_session_group, parent, false));
        }
        return new SessionHolder(inflater.inflate(R.layout.item_session, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).label.setText((String) row);
            return;
        }

        SessionHolder sessionHolder = (SessionHolder) holder;
        FocusSession session = (FocusSession) row;
        sessionHolder.time.setText(session.time);
        sessionHolder.title.setText(session.title);
        sessionHolder.duration.setText(session.minutes + " min");
        sessionHolder.status.setText(session.completed
                ? R.string.history_completed
                : R.string.history_stopped);
        sessionHolder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                session.completed ? R.color.ink_muted : R.color.danger));
        sessionHolder.dot.setBackgroundResource(session.completed
                ? R.drawable.bg_dot_accent
                : R.drawable.bg_dot_muted);
        holder.itemView.setOnClickListener(v -> listener.onSessionClick(session));
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

    static class SessionHolder extends RecyclerView.ViewHolder {
        final View dot;
        final TextView time;
        final TextView title;
        final TextView status;
        final TextView duration;

        SessionHolder(@NonNull View itemView) {
            super(itemView);
            dot = itemView.findViewById(R.id.sessionDot);
            time = itemView.findViewById(R.id.sessionTime);
            title = itemView.findViewById(R.id.sessionTitle);
            status = itemView.findViewById(R.id.sessionStatus);
            duration = itemView.findViewById(R.id.sessionDuration);
        }
    }
}
