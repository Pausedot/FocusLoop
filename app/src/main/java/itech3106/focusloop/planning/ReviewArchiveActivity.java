package itech3106.focusloop.planning;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.adapter.ReviewAdapter;
import itech3106.focusloop.data.Review;
import itech3106.focusloop.data.ReviewStore;

public class ReviewArchiveActivity extends ModuleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_archive);
        bindChrome(R.string.title_review_archive);

        List<Review> reviews = new ReviewStore(this).recent();
        RecyclerView list = findViewById(R.id.reviewList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new ReviewAdapter(reviews));

        ((TextView) findViewById(R.id.archiveSummary))
                .setText(getString(R.string.archive_summary_format, reviews.size()));

        boolean isEmpty = reviews.isEmpty();
        list.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        findViewById(R.id.emptyState).setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_plan;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_review_archive;
    }
}
