package itech3106.focusloop.planning;

import android.content.Intent;
import android.os.Bundle;

import itech3106.focusloop.BaseActivity;
import itech3106.focusloop.R;

public class DailyReviewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_review);
        bindChrome(R.string.title_review);

        findViewById(R.id.btnSaveReview).setOnClickListener(v -> finish());
        findViewById(R.id.btnViewStats).setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));
    }
}
