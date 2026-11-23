package itech3106.focusloop;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

import itech3106.focusloop.focus.BreakActivity;
import itech3106.focusloop.focus.FiveSecondActivity;
import itech3106.focusloop.focus.FocusHistoryActivity;
import itech3106.focusloop.focus.PomodoroActivity;
import itech3106.focusloop.focus.TimerSettingsActivity;
import itech3106.focusloop.planning.DailyReviewActivity;
import itech3106.focusloop.planning.ReviewArchiveActivity;
import itech3106.focusloop.planning.StatsActivity;
import itech3106.focusloop.planning.TimeBlocksActivity;
import itech3106.focusloop.planning.TwoMinuteActivity;
import itech3106.focusloop.tasks.DoneActivity;
import itech3106.focusloop.tasks.MatrixActivity;
import itech3106.focusloop.tasks.TaskEditActivity;
import itech3106.focusloop.tasks.TaskInboxActivity;

/**
 * Every module screen shares one visible menu: a pill in the toolbar opens a bottom sheet
 * with the module's screens and the current screen's actions.
 *
 * <p>A1 used the standard overflow, which hid the module's other screens behind three
 * dots. The sheet keeps the menu as the navigation surface, but makes it visible and
 * explains what each screen is for. The menu resource is still the source of actions, so
 * the "must use menus" requirement is met without a hidden overflow.
 */
public abstract class ModuleActivity extends BaseActivity {

    private static final int MODULE_TASKS = 0;
    private static final int MODULE_FOCUS = 1;
    private static final int MODULE_PLANNING = 2;

    private MaterialButton menuButton;

    protected abstract int menuRes();

    /** Menu id of this screen, marked as current in the sheet. 0 when there is none. */
    protected int selfMenuItem() {
        return 0;
    }

    /** Screens that should not offer a particular action (for example, sorting on Matrix). */
    protected boolean showAction(int id) {
        return true;
    }

    @Override
    protected void bindChrome(int titleRes) {
        super.bindChrome(titleRes);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            addMenuButton(toolbar);
        }
    }

    private void addMenuButton(MaterialToolbar toolbar) {
        menuButton = new MaterialButton(this);
        menuButton.setText(R.string.menu_button);
        menuButton.setTextSize(13);
        menuButton.setAllCaps(false);
        menuButton.setTextColor(ContextCompat.getColor(this, R.color.accent));
        menuButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.accent_soft)));
        menuButton.setCornerRadius(dp(18));
        menuButton.setInsetTop(0);
        menuButton.setInsetBottom(0);
        menuButton.setMinHeight(0);
        menuButton.setMinimumHeight(0);
        menuButton.setMinWidth(0);
        menuButton.setMinimumWidth(0);
        menuButton.setPadding(dp(14), 0, dp(14), 0);
        menuButton.setContentDescription(getString(R.string.menu_button));
        menuButton.setOnClickListener(v -> showModuleMenu());

        Toolbar.LayoutParams params = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT, dp(34),
                Gravity.END | Gravity.CENTER_VERTICAL);
        params.setMarginEnd(dp(8));
        toolbar.addView(menuButton, params);
    }

    private void showModuleMenu() {
        if (menuButton == null) {
            return;
        }
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.sheet_module_menu, null);
        dialog.setContentView(content);

        ((TextView) content.findViewById(R.id.menuSheetTitle))
                .setText(moduleTitleRes());

        LinearLayout screens = content.findViewById(R.id.menuScreens);
        for (Screen screen : screensForModule()) {
            boolean current = screen.id == selfMenuItem();
            addRow(screens, getString(screen.title), getString(screen.subtitle), current, v -> {
                if (!current) {
                    startScreen(screen);
                }
                dialog.dismiss();
            });
        }

        LinearLayout actions = content.findViewById(R.id.menuActions);
        Menu menu = new PopupMenu(this, menuButton).getMenu();
        getMenuInflater().inflate(menuRes(), menu);

        int actionCount = 0;
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            int id = item.getItemId();
            if (isScreen(id) || !showAction(id)) {
                continue;
            }
            actionCount++;
            addRow(actions, item.getTitle().toString(), null, false, v -> {
                onOptionsItemSelected(item);
                dialog.dismiss();
            });
        }
        if (actionCount == 0) {
            content.findViewById(R.id.menuActionsLabel).setVisibility(View.GONE);
            actions.setVisibility(View.GONE);
        }

        dialog.show();
    }

    private void addRow(LinearLayout parent, String title, String subtitle, boolean current,
                        View.OnClickListener click) {
        View row = getLayoutInflater().inflate(R.layout.item_menu_row, parent, false);
        ((TextView) row.findViewById(R.id.menuRowTitle)).setText(title);

        TextView sub = row.findViewById(R.id.menuRowSubtitle);
        if (subtitle == null || subtitle.isEmpty()) {
            sub.setVisibility(View.GONE);
        } else {
            sub.setText(subtitle);
        }

        row.findViewById(R.id.menuRowCheck).setVisibility(current ? View.VISIBLE : View.GONE);
        row.setOnClickListener(click);
        parent.addView(row);
    }

    private int module() {
        int menu = menuRes();
        if (menu == R.menu.menu_tasks || menu == R.menu.menu_task_detail) {
            return MODULE_TASKS;
        }
        if (menu == R.menu.menu_focus) {
            return MODULE_FOCUS;
        }
        return MODULE_PLANNING;
    }

    private int moduleTitleRes() {
        switch (module()) {
            case MODULE_TASKS:
                return R.string.title_tasks;
            case MODULE_FOCUS:
                return R.string.title_focus;
            default:
                return R.string.title_plan;
        }
    }

    private List<Screen> screensForModule() {
        switch (module()) {
            case MODULE_TASKS:
                return Arrays.asList(
                        new Screen(R.id.action_inbox, R.string.tab_inbox,
                                R.string.menu_desc_inbox, TaskInboxActivity.class),
                        new Screen(R.id.action_matrix, R.string.tab_matrix,
                                R.string.menu_desc_matrix, MatrixActivity.class),
                        new Screen(R.id.action_done, R.string.tab_done,
                                R.string.menu_desc_done, DoneActivity.class));
            case MODULE_FOCUS:
                return Arrays.asList(
                        new Screen(R.id.action_pomodoro, R.string.tab_timer,
                                R.string.menu_desc_timer, PomodoroActivity.class),
                        new Screen(R.id.action_break, R.string.tab_break,
                                R.string.menu_desc_break, BreakActivity.class),
                        new Screen(R.id.action_history, R.string.tab_history,
                                R.string.menu_desc_history, FocusHistoryActivity.class),
                        new Screen(R.id.action_timer_settings, R.string.tab_settings,
                                R.string.menu_desc_settings, TimerSettingsActivity.class));
            default:
                return Arrays.asList(
                        new Screen(R.id.action_time_blocks, R.string.tab_plan,
                                R.string.menu_desc_plan, TimeBlocksActivity.class),
                        new Screen(R.id.action_two_minute, R.string.tab_two_minute,
                                R.string.menu_desc_two, TwoMinuteActivity.class),
                        new Screen(R.id.action_daily_review, R.string.tab_review,
                                R.string.menu_desc_review, DailyReviewActivity.class),
                        new Screen(R.id.action_stats, R.string.tab_stats,
                                R.string.menu_desc_stats, StatsActivity.class),
                        new Screen(R.id.action_review_archive, R.string.tab_archive,
                                R.string.menu_desc_archive, ReviewArchiveActivity.class));
        }
    }

    private boolean isScreen(int id) {
        for (Screen screen : screensForModule()) {
            if (screen.id == id) {
                return true;
            }
        }
        return false;
    }

    private void startScreen(Screen screen) {
        Intent intent = new Intent(this, screen.activity);
        intent.putExtra(Extras.TASK_ID, getIntent().getStringExtra(Extras.TASK_ID));
        intent.putExtra(Extras.TASK_TITLE, getIntent().getStringExtra(Extras.TASK_TITLE));
        startActivity(intent);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /**
     * Up is hierarchical: secondary screens go to the module's default screen, and the
     * default screen goes to Main. System back keeps its usual "previous screen" meaning.
     */
    private void goUp() {
        Screen home = screensForModule().get(0);
        Intent intent;
        if (home.id == selfMenuItem()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, home.activity);
            intent.putExtra(Extras.TASK_ID, getIntent().getStringExtra(Extras.TASK_ID));
            intent.putExtra(Extras.TASK_TITLE, getIntent().getStringExtra(Extras.TASK_TITLE));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    private static final class Screen {
        final int id;
        final int title;
        final int subtitle;
        final Class<?> activity;

        Screen(int id, int title, int subtitle, Class<?> activity) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.activity = activity;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goUp();
            return true;
        }
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
        if (id == R.id.action_done) {
            startActivity(new Intent(this, DoneActivity.class));
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
            Intent intent = new Intent(this, PomodoroActivity.class);
            intent.putExtra(Extras.TASK_ID, getIntent().getStringExtra(Extras.TASK_ID));
            intent.putExtra(Extras.TASK_TITLE, getIntent().getStringExtra(Extras.TASK_TITLE));
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_five_second) {
            Intent intent = new Intent(this, FiveSecondActivity.class);
            intent.putExtra(Extras.TASK_ID, getIntent().getStringExtra(Extras.TASK_ID));
            intent.putExtra(Extras.TASK_TITLE, getIntent().getStringExtra(Extras.TASK_TITLE));
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_break) {
            Intent intent = new Intent(this, BreakActivity.class);
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
        if (id == R.id.action_review_archive) {
            startActivity(new Intent(this, ReviewArchiveActivity.class));
            return true;
        }
        if (id == R.id.action_plan_tomorrow) {
            Intent intent = new Intent(this, TimeBlocksActivity.class);
            intent.putExtra(Extras.DAY_OFFSET, 1);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
