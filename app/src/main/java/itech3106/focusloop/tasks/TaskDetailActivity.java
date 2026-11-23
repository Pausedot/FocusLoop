package itech3106.focusloop.tasks;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import itech3106.focusloop.Extras;
import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;

/** Phone host for the task detail fragment. The tablet uses the two-pane inbox instead. */
public class TaskDetailActivity extends ModuleActivity implements TaskDetailFragment.Listener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail_host);
        bindChrome(R.string.title_task);

        String taskId = getIntent().getStringExtra(Extras.TASK_ID);
        if (taskId == null) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detailContainer, TaskDetailFragment.newInstance(taskId), "detail")
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("detail");
        if (fragment instanceof TaskDetailFragment) {
            ((TaskDetailFragment) fragment).reload();
        }
    }

    @Override
    public void onTaskCompleted() {
        finish();
    }

    @Override
    public void onTaskEdited() {
        // The fragment reloads in onResume; nothing else to do here.
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_task_detail;
    }
}
