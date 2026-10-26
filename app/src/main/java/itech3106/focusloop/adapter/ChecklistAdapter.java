package itech3106.focusloop.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

import itech3106.focusloop.R;
import itech3106.focusloop.data.ChecklistItem;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.Holder> {

    private final List<ChecklistItem> items;

    public ChecklistAdapter(List<ChecklistItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checklist, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChecklistItem item = items.get(position);
        holder.check.setChecked(item.done);
        holder.text.setText(item.text);
        int flags = item.done
                ? holder.text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                : holder.text.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG;
        holder.text.setPaintFlags(flags);
        holder.text.setAlpha(item.done ? 0.45f : 1f);

        holder.check.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            item.done = holder.check.isChecked();
            notifyItemChanged(pos);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final MaterialCheckBox check;
        final TextView text;

        Holder(@NonNull View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.check);
            text = itemView.findViewById(R.id.checkText);
        }
    }
}
