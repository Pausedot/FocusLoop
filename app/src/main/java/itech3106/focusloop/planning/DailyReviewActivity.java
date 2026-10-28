package itech3106.focusloop.planning;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;

public class DailyReviewActivity extends ModuleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_review);
        bindChrome(R.string.title_review);

        // Show today's date instead of the fixed caption that ships in strings.xml.
        String today = new SimpleDateFormat("EEEE · d MMMM", Locale.ENGLISH)
                .format(Calendar.getInstance().getTime())
                .toUpperCase(Locale.ENGLISH);
        ((TextView) findViewById(R.id.reviewDate)).setText(today);

        findViewById(R.id.btnSaveReview).setOnClickListener(v -> finish());
        findViewById(R.id.btnViewStats).setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_plan;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_daily_review;
    }
}
