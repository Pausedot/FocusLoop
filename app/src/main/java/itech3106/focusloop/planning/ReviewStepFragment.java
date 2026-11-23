package itech3106.focusloop.planning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import itech3106.focusloop.R;

/** One question of the daily review. The same class is used for all three steps. */
public class ReviewStepFragment extends Fragment {

    private static final String ARG_STEP = "step";
    private static final String ARG_ANSWER = "answer";

    private TextInputEditText edit;

    public static ReviewStepFragment newInstance(int step, String answer) {
        ReviewStepFragment fragment = new ReviewStepFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STEP, step);
        args.putString(ARG_ANSWER, answer == null ? "" : answer);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review_step, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int step = getArguments() == null ? 0 : getArguments().getInt(ARG_STEP);
        String answer = getArguments() == null ? "" : getArguments().getString(ARG_ANSWER, "");

        int title;
        int hint;
        switch (step) {
            case 1:
                title = R.string.review_q2;
                hint = R.string.hint_review_q2;
                break;
            case 2:
                title = R.string.review_q3;
                hint = R.string.hint_review_q3;
                break;
            default:
                title = R.string.review_q1;
                hint = R.string.hint_review_q1;
                break;
        }

        ((TextView) view.findViewById(R.id.reviewStepTitle)).setText(title);
        ((TextInputLayout) view.findViewById(R.id.reviewStepInput)).setHint(getString(hint));
        edit = view.findViewById(R.id.reviewStepEdit);
        edit.setText(answer);
    }

    public String answer() {
        return edit == null || edit.getText() == null
                ? "" : edit.getText().toString().trim();
    }

    public void setAnswer(String answer) {
        if (edit != null) {
            edit.setText(answer == null ? "" : answer);
        }
    }
}
