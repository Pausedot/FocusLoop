package itech3106.focusloop.planning;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import itech3106.focusloop.ModuleActivity;
import itech3106.focusloop.R;
import itech3106.focusloop.adapter.ChecklistAdapter;
import itech3106.focusloop.data.ChecklistItem;
import itech3106.focusloop.data.DemoData;

public class TwoMinuteActivity extends ModuleActivity {

    private final List<ChecklistItem> items = new ArrayList<>();
    private ChecklistAdapter adapter;
    private RecyclerView list;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_minute);
        bindChrome(R.string.title_two_minute);

        items.addAll(DemoData.checklist());
        adapter = new ChecklistAdapter(items);
        list = findViewById(R.id.twoList);
        emptyState = findViewById(R.id.emptyState);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.btnTwoAdd).setOnClickListener(v -> addItem());
        findViewById(R.id.btnClearDone).setOnClickListener(v -> clearDone());

        updateFooter();
        updateEmptyState();
    }

    private void addItem() {
        TextInputEditText input = findViewById(R.id.twoInput);
        String text = input.getText() == null ? "" : input.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        items.add(0, new ChecklistItem(text, false));
        adapter.notifyItemInserted(0);
        list.scrollToPosition(0);
        input.setText("");
        updateFooter();
        updateEmptyState();

        InputMethodManager keyboard = getSystemService(InputMethodManager.class);
        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
        }
    }

    private void clearDone() {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i).done) {
                items.remove(i);
            }
        }
        adapter.notifyDataSetChanged();
        updateFooter();
        updateEmptyState();
    }

    private void updateFooter() {
        int left = 0;
        for (ChecklistItem item : items) {
            if (!item.done) {
                left++;
            }
        }
        ((TextView) findViewById(R.id.twoCount)).setText(getString(R.string.two_left, left));
    }

    private void updateEmptyState() {
        boolean isEmpty = items.isEmpty();
        list.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    protected int menuRes() {
        return R.menu.menu_plan;
    }

    @Override
    protected int selfMenuItem() {
        return R.id.action_two_minute;
    }
}
