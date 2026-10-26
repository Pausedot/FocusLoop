package itech3106.focusloop;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import itech3106.focusloop.focus.FiveSecondActivity;
import itech3106.focusloop.focus.FocusHistoryActivity;
import itech3106.focusloop.focus.PomodoroActivity;
import itech3106.focusloop.focus.TimerSettingsActivity;
import itech3106.focusloop.planning.DailyReviewActivity;
import itech3106.focusloop.planning.StatsActivity;
import itech3106.focusloop.planning.TimeBlocksActivity;
import itech3106.focusloop.planning.TwoMinuteActivity;
import itech3106.focusloop.tasks.MatrixActivity;
import itech3106.focusloop.tasks.TaskEditActivity;
import itech3106.focusloop.tasks.TaskInboxActivity;

/**
 * Every module screen shares the same overflow menu; each screen hides its own entry.
 * This is the "must use menus" requirement, and it keeps module navigation in one place.
 */
public abstract class ModuleActivity extends BaseActivity {

    protected abstract int menuRes();

    /** Menu id of this screen, removed from its own overflow. 0 when nothing should be removed. */
    protected int selfMenuItem() {
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(menuRes(), menu);
        int self = selfMenuItem();
        if (self != 0) {
            menu.removeItem(self);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_inbox) {
            startActivity(new Intent(this, TaskInboxActivity.class));
            return true;
        }
        if (id == R.id.action_new_task) {
            startActivity(new Intent(this, TaskEditActivity.class));
            return true;
        }
        if (id == R.id.action_matrix) {
            startActivity(new Intent(this, MatrixActivity.class));
            return true;
        }
        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, TaskEditActivity.class);
            intent.putExtra(Extras.TASK_ID, getIntent().getStringExtra(Extras.TASK_ID));
            intent.putExtra(Extras.TASK_TITLE, getIntent().getStringExtra(Extras.TASK_TITLE));
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_pomodoro) {
            startActivity(new Intent(this, PomodoroActivity.class));
            return true;
        }
        if (id == R.id.action_five_second) {
            Intent intent = new Intent(this, FiveSecondActivity.class);
            intent.putExtra(Extras.TASK_ID, getIntent().getStringExtra(Extras.TASK_ID));
            intent.putExtra(Extras.TASK_TITLE, getIntent().getStringExtra(Extras.TASK_TITLE));
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_history) {
            startActivity(new Intent(this, FocusHistoryActivity.class));
            return true;
        }
        if (id == R.id.action_timer_settings) {
            startActivity(new Intent(this, TimerSettingsActivity.class));
            return true;
        }
        if (id == R.id.action_time_blocks) {
            startActivity(new Intent(this, TimeBlocksActivity.class));
            return true;
        }
        if (id == R.id.action_two_minute) {
            startActivity(new Intent(this, TwoMinuteActivity.class));
            return true;
        }
        if (id == R.id.action_daily_review) {
            startActivity(new Intent(this, DailyReviewActivity.class));
            return true;
        }
        if (id == R.id.action_stats) {
            startActivity(new Intent(this, StatsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
