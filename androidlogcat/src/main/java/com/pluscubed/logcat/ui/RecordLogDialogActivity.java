package com.pluscubed.logcat.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pluscubed.logcat.R;
import com.pluscubed.logcat.data.FilterQueryWithLevel;
import com.pluscubed.logcat.helper.DialogHelper;
import com.pluscubed.logcat.helper.PreferenceHelper;
import com.pluscubed.logcat.helper.WidgetHelper;
import com.pluscubed.logcat.util.Callback;

import java.util.Arrays;
import java.util.List;

public class RecordLogDialogActivity extends AppCompatActivity {

    public static final String EXTRA_QUERY_SUGGESTIONS = "suggestions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showDialog();
    }

    private void showDialog() {
        final String[] suggestions = (getIntent() != null && getIntent().hasExtra(EXTRA_QUERY_SUGGESTIONS))
                ? getIntent().getStringArrayExtra(EXTRA_QUERY_SUGGESTIONS) : new String[]{};

        DialogFragment fragment = ShowRecordLogDialog.newInstance(suggestions);
        fragment.show(getFragmentManager(), "showRecordLogDialog");
    }

    public static class ShowRecordLogDialog extends DialogFragment {

        public static final String QUERY_SUGGESTIONS = "suggestions";

        public static ShowRecordLogDialog newInstance(String[] suggestions) {
            ShowRecordLogDialog dialog = new ShowRecordLogDialog();
            Bundle args = new Bundle();
            args.putStringArray(QUERY_SUGGESTIONS, suggestions);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public void onResume() {
            super.onResume();
            getDialog().setCancelable(false);
            getDialog().setCanceledOnTouchOutside(false);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //noinspection ConstantConditions
            final List<String> suggestions = Arrays.asList(getArguments().getStringArray(QUERY_SUGGESTIONS));

            String logFilename = DialogHelper.createLogFilename();

            String defaultLogLevel = Character.toString(PreferenceHelper.getDefaultLogLevelPreference(getActivity()));
            final StringBuilder queryFilterText = new StringBuilder();
            final StringBuilder logLevelText = new StringBuilder(defaultLogLevel);
            final Activity activity = getActivity();
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.record_log)
                    .content(R.string.enter_filename)
                    .input("", logFilename, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog materialDialog, CharSequence charSequence) {
                            if (DialogHelper.isInvalidFilename(charSequence)) {
                                Toast.makeText(getActivity(), R.string.enter_good_filename, Toast.LENGTH_SHORT).show();
                            } else {
                                materialDialog.dismiss();
                                String filename = charSequence.toString();
                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.finish();
                                    }
                                };
                                DialogHelper.startRecordingWithProgressDialog(filename,
                                        queryFilterText.toString(), logLevelText.toString(), runnable, getActivity());
                            }
                        }
                    })
                    .neutralText(R.string.text_filter_ellipsis)
                    .negativeText(android.R.string.cancel)
                    .autoDismiss(false)
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            WidgetHelper.updateWidgets(getActivity());
                            switch (which) {
                                case NEUTRAL:
                                    DialogHelper.showFilterDialogForRecording(getActivity(), queryFilterText.toString(),
                                            logLevelText.toString(), suggestions,
                                            new Callback<FilterQueryWithLevel>() {
                                                @Override
                                                public void onCallback(FilterQueryWithLevel result) {
                                                    queryFilterText.replace(0, queryFilterText.length(), result.getFilterQuery());
                                                    logLevelText.replace(0, logLevelText.length(), result.getLogLevel());
                                                }
                                            });
                                    break;
                                case NEGATIVE:
                                    dialog.dismiss();
                                    getActivity().finish();
                                    break;
                            }
                        }
                    }).build();
            //noinspection ConstantConditions
            DialogHelper.initFilenameInputDialog(dialog);

            return dialog;
        }
    }
}
