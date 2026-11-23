package itech3106.focusloop.focus;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.fragment.app.Fragment;

import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.data.FocusSession;

/**
 * Hosts the history list and, on a tablet, the session detail beside it. On a phone the
 * detail replaces the list and goes on the back stack.
 */
public class FocusHistoryActivity extends ModuleActivity implements HistoryListFragment.Listener {

    private boolean twoPane;
    private View placeholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_history_host);
        bindChrome(R.string.title_history);

        twoPane = findViewById(R.id.detailContainer) != null;
        placeholder = findViewById(R.id.historyPlaceholder);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.listContainer, new HistoryListFragment(), "list")
                    .commit();
        } else if (twoPane
                && getSupportFragmentManager().findFragmentByTag("session") != null
                && placeholder != null) {
            placeholder.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSessionSelected(FocusSession session) {
        Fragment detail = SessionDetailFragment.newInstance(session.id);
        if (twoPane) {
            if (placeholder != null) {
                placeholder.setVisibility(View.GONE);
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detailContainer, detail, "session")
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.listContainer, detail, "session")
                    .addToBackStack("session")
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home
                && getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_focus;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_history;
    }
}
