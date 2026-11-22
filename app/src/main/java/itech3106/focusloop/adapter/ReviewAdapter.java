package itech3106.focusloop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import itech3106.focusloop.R;
import itech3106.focusloop.data.Dates;
import itech3106.focusloop.data.Review;

/** One read-only card per saved daily review. */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.Holder> {

    private final List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Review review = reviews.get(position);
        holder.date.setText(Dates.longDate(review.epochDay).toUpperCase(Locale.ENGLISH));
        holder.tomorrow.setText(review.tomorrow.isEmpty()
                ? holder.itemView.getContext().getString(R.string.dialog_no_task)
                : review.tomorrow);
        holder.wentWell.setText(review.wentWell);

        boolean hasObstacle = !review.obstacle.isEmpty();
        holder.obstacleLabel.setVisibility(hasObstacle ? View.VISIBLE : View.GONE);
        holder.obstacle.setVisibility(hasObstacle ? View.VISIBLE : View.GONE);
        holder.obstacle.setText(review.obstacle);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView date;
        final TextView tomorrow;
        final TextView wentWell;
        final TextView obstacleLabel;
        final TextView obstacle;

        Holder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.reviewDate);
            tomorrow = itemView.findViewById(R.id.tomorrowValue);
            wentWell = itemView.findViewById(R.id.wentWellValue);
            obstacleLabel = itemView.findViewById(R.id.obstacleLabel);
            obstacle = itemView.findViewById(R.id.obstacleValue);
        }
    }
}
