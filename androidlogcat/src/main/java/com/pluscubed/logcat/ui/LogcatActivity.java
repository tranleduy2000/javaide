package com.pluscubed.logcat.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.MatrixCursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filter.FilterListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.pluscubed.logcat.LogcatRecordingService;
import com.pluscubed.logcat.R;
import com.pluscubed.logcat.data.ColorScheme;
import com.pluscubed.logcat.data.FilterAdapter;
import com.pluscubed.logcat.data.LogFileAdapter;
import com.pluscubed.logcat.data.LogLine;
import com.pluscubed.logcat.data.LogLineAdapter;
import com.pluscubed.logcat.data.LogLineViewHolder;
import com.pluscubed.logcat.data.SavedLog;
import com.pluscubed.logcat.data.SearchCriteria;
import com.pluscubed.logcat.data.SendLogDetails;
import com.pluscubed.logcat.data.SortedFilterArrayAdapter;
import com.pluscubed.logcat.databinding.ActivityLogcatBinding;
import com.pluscubed.logcat.db.CatlogDBHelper;
import com.pluscubed.logcat.db.FilterItem;
import com.pluscubed.logcat.helper.BuildHelper;
import com.pluscubed.logcat.helper.DialogHelper;
import com.pluscubed.logcat.helper.PreferenceHelper;
import com.pluscubed.logcat.helper.SaveLogHelper;
import com.pluscubed.logcat.helper.ServiceHelper;
import com.pluscubed.logcat.helper.UpdateHelper;
import com.pluscubed.logcat.intents.Intents;
import com.pluscubed.logcat.reader.LogcatReader;
import com.pluscubed.logcat.reader.LogcatReaderLoader;
import com.pluscubed.logcat.util.ArrayUtil;
import com.pluscubed.logcat.util.LogLineAdapterUtil;
import com.pluscubed.logcat.util.StringUtil;
import com.pluscubed.logcat.util.UtilLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.pluscubed.logcat.data.LogLineViewHolder.CONTEXT_MENU_COPY_ID;
import static com.pluscubed.logcat.data.LogLineViewHolder.CONTEXT_MENU_FILTER_ID;


public class LogcatActivity extends AppCompatActivity implements FilterListener, LogLineViewHolder.OnClickListener {

    private static final int REQUEST_CODE_SETTINGS = 1;

    // how often to check to see if we've gone over the max size
    private static final int UPDATE_CHECK_INTERVAL = 200;

    // how many suggestions to keep in the autosuggestions text
    private static final int MAX_NUM_SUGGESTIONS = 1000;

    // id requests for access to sdcard
    private static final int DELETE_SAVED_LOG_REQUEST = 1;
    private static final int SEND_LOG_ID_REQUEST = 2;
    private static final int SAVE_LOG_REQUEST = 3;
    private static final int OPEN_LOG_REQUEST = 4;
    private static final int COMPLETE_PARTIAL_SELECT_REQUEST = 5;
    private static final int SHOW_RECORD_LOG_REQUEST = 6;
    private static final int SHOW_RECORD_LOG_REQUEST_SHORTCUT = 7;

    private static UtilLogger log = new UtilLogger(LogcatActivity.class);

    private LogLineAdapter mLogListAdapter;
    private LogReaderAsyncTask mTask;

    private String mSearchingString;

    private boolean mAutoscrollToBottom = true;
    private boolean mCollapsedMode;

    private boolean mDynamicallyEnteringSearchText;
    private boolean partialSelectMode;
    private List<LogLine> partiallySelectedLogLines = new ArrayList<>(2);

    private Set<String> mSearchSuggestionsSet = new HashSet<>();
    private CursorAdapter mSearchSuggestionsAdapter;

    private String mCurrentlyOpenLog = null;

    private Handler mHandler;
    private MenuItem mSearchViewMenuItem;

    private ActivityLogcatBinding binding;

    public static void startChooser(Context context, String subject, String body, SendLogDetails.AttachmentType attachmentType, File attachment) {

        Intent actionSendIntent = new Intent(Intent.ACTION_SEND);

        actionSendIntent.setType(attachmentType.getMimeType());
        actionSendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (!body.isEmpty()) {
            actionSendIntent.putExtra(Intent.EXTRA_TEXT, body);
        }
        if (attachment != null) {
            Uri uri = Uri.fromFile(attachment);
            log.d("uri is: %s", uri);
            actionSendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        context.startActivity(Intent.createChooser(actionSendIntent, context.getResources().getText(R.string.send_log_title)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_LONG).show();
            return;
        }

        switch (requestCode) {
            case DELETE_SAVED_LOG_REQUEST:
                startDeleteSavedLogsDialog();
                break;
            case SEND_LOG_ID_REQUEST:
                showSendLogDialog();
                break;
            case SAVE_LOG_REQUEST:
                showSaveLogDialog();
                break;
            case OPEN_LOG_REQUEST:
                showOpenLogFileDialog();
                break;
            case COMPLETE_PARTIAL_SELECT_REQUEST:
                completePartialSelect();
                break;
            case SHOW_RECORD_LOG_REQUEST:
                showRecordLogDialog();
                break;
            case SHOW_RECORD_LOG_REQUEST_SHORTCUT:
                handleShortcuts("record");
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_logcat);

        LogLine.isScrubberEnabled = PreferenceHelper.isScrubberEnabled(this);

        handleShortcuts(getIntent().getStringExtra("shortcut_action"));

        mHandler = new Handler(Looper.getMainLooper());

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.stopRecordingLog(LogcatActivity.this);
            }
        });

        binding.list.setLayoutManager(new LinearLayoutManager(this));

        binding.list.setItemAnimator(null);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_actionbar));

        mCollapsedMode = !PreferenceHelper.getExpandedByDefaultPreference(this);

        log.d("initial collapsed mode is %s", mCollapsedMode);

        mSearchSuggestionsAdapter = new SimpleCursorAdapter(this,
                R.layout.list_item_dropdown,
                null,
                new String[]{"suggestion"},
                new int[]{android.R.id.text1},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        setUpAdapter();
        updateBackgroundColor();
        runUpdatesIfNecessaryAndShowWelcomeMessage();
    }

    private void handleShortcuts(String action) {
        if (action == null) return;

        switch (action) {
            case "record":
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            SHOW_RECORD_LOG_REQUEST_SHORTCUT);
                    return;
                }

                String logFilename = DialogHelper.createLogFilename();
                String defaultLogLevel = Character.toString(PreferenceHelper.getDefaultLogLevelPreference(this));

                DialogHelper.startRecordingWithProgressDialog(logFilename, "", defaultLogLevel, new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, this);

                break;
        }
    }

    private void runUpdatesIfNecessaryAndShowWelcomeMessage() {

        if (UpdateHelper.areUpdatesNecessary(this)) {
            // show progress dialog while updates are running

            final MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .content(R.string.dialog_loading_updates)
                    .progress(true, 0)
                    .show();

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    UpdateHelper.runUpdatesIfNecessary(LogcatActivity.this);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    startLog();
                }


            }.execute((Void) null);

        } else {
            startLog();
        }

    }

    private void addFiltersToSuggestions() {
        CatlogDBHelper dbHelper = null;
        try {
            dbHelper = new CatlogDBHelper(this);

            for (FilterItem filterItem : dbHelper.findFilterItems()) {
                addToAutocompleteSuggestions(filterItem.getText());
            }
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }

    }

    private void startLog() {

        Intent intent = getIntent();

        if (intent == null || !intent.hasExtra("filename")) {
            startMainLog();
        } else {
            String filename = intent.getStringExtra("filename");
            openLogFile(filename);
        }

        doAfterInitialMessage(getIntent());


    }

    private void doAfterInitialMessage(Intent intent) {

        // handle an intent that was sent from an external application

        if (intent != null && Intents.ACTION_LAUNCH.equals(intent.getAction())) {

            String filter = intent.getStringExtra(Intents.EXTRA_FILTER);
            String level = intent.getStringExtra(Intents.EXTRA_LEVEL);

            if (!TextUtils.isEmpty(filter)) {
                setSearchText(filter);
            }


            if (!TextUtils.isEmpty(level)) {
                CharSequence[] logLevels = getResources().getStringArray(R.array.log_levels_values);
                int logLevelLimit = ArrayUtil.indexOf(logLevels, level.toUpperCase(Locale.US));

                if (logLevelLimit == -1) {
                    String invalidLevel = getString(R.string.toast_invalid_level, level);
                    Toast.makeText(this, invalidLevel, Toast.LENGTH_LONG).show();
                } else {
                    mLogListAdapter.setLogLevelLimit(logLevelLimit);
                    logLevelChanged();
                }

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mLogListAdapter.getItemCount() > 0) {
            // scroll to bottom, since for some reason it always scrolls to the top, which is annoying
            scrollToBottom();
        }

        boolean recordingInProgress = ServiceHelper.checkIfServiceIsRunning(getApplicationContext(), LogcatRecordingService.class);
        binding.fab.setVisibility(recordingInProgress ? View.VISIBLE : View.GONE);
    }

    private void restartMainLog() {
        mLogListAdapter.clear();

        startMainLog();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        doAfterInitialMessage(intent);

        // launched from the widget or notification
        if (intent != null && !Intents.ACTION_LAUNCH.equals(intent.getAction()) && intent.hasExtra("filename")) {
            String filename = intent.getStringExtra("filename");
            openLogFile(filename);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log.d("onActivityResult()");

        // preferences may have changed
        PreferenceHelper.clearCache();

        mCollapsedMode = !PreferenceHelper.getExpandedByDefaultPreference(getApplicationContext());


        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
            onSettingsActivityResult(data);
        }
        mLogListAdapter.notifyDataSetChanged();
        updateBackgroundColor();
        updateUiForFilename();
    }

    private void onSettingsActivityResult(final Intent data) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateBackgroundColor();
                if (data.hasExtra("bufferChanged") && data.getBooleanExtra("bufferChanged", false)
                        && mCurrentlyOpenLog == null) {
                    // log buffer changed, so update list
                    restartMainLog();
                } else {
                    // settings activity returned - text size might have changed, so update list
                    expandOrCollapseAll(false);
                    mLogListAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void startMainLog() {
        Runnable mainLogRunnable = new Runnable() {
            @Override
            public void run() {
                if (mLogListAdapter != null) {
                    mLogListAdapter.clear();
                }
                mTask = new LogReaderAsyncTask();
                mTask.execute((Void) null);
            }
        };

        if (mTask != null) {
            // do only after current log is depleted, to avoid splicing the streams together
            // (Don't cross the streams!)
            mTask.unpause();
            mTask.setOnFinished(mainLogRunnable);
            mTask.killReader();
            mTask = null;
        } else {
            // no main log currently running; just start up the main log now
            mainLogRunnable.run();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        log.d("onPause() called");

        cancelPartialSelect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.d("onDestroy() called");

        if (mTask != null) {
            mTask.unpause();
            mTask.killReader();
            mTask = null;
        }
    }

    private void populateSuggestionsAdapter(String query) {
        final MatrixCursor c = new MatrixCursor(new String[]{BaseColumns._ID, "suggestion"});
        List<String> suggestionsForQuery = getSuggestionsForQuery(query);
        for (int i = 0, suggestionsForQuerySize = suggestionsForQuery.size(); i < suggestionsForQuerySize; i++) {
            String suggestion = suggestionsForQuery.get(i);
            c.addRow(new Object[]{i, suggestion});
        }
        mSearchSuggestionsAdapter.changeCursor(c);
    }

    private List<String> getSuggestionsForQuery(String query) {
        List<String> suggestions = new ArrayList<>(mSearchSuggestionsSet);
        Collections.sort(suggestions, String.CASE_INSENSITIVE_ORDER);
        List<String> actualSuggestions = new ArrayList<>();
        if (query != null) {
            for (String suggestion : suggestions) {
                if (suggestion.toLowerCase().startsWith(query.toLowerCase())) {
                    actualSuggestions.add(suggestion);
                }
            }
        }
        return actualSuggestions;
    }

    @Override
    public void onBackPressed() {
        if (mSearchViewMenuItem.isActionViewExpanded()) {
            mSearchViewMenuItem.collapseActionView();
        } else if (mCurrentlyOpenLog != null) {
            startMainLog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateDarkOrLightMenuItems(this, menu);

        boolean showingMainLog = (mTask != null);

        MenuItem item = menu.findItem(R.id.menu_expand_all);
        if (mCollapsedMode) {
            item.setIcon(R.drawable.ic_expand_more_white_24dp);
            item.setTitle(R.string.expand_all);
        } else {
            item.setIcon(R.drawable.ic_expand_less_white_24dp);
            item.setTitle(R.string.collapse_all);
        }

        MenuItem clear = menu.findItem(R.id.menu_clear);
        MenuItem pause = menu.findItem(R.id.menu_play_pause);
        clear.setVisible(mCurrentlyOpenLog == null);
        pause.setVisible(mCurrentlyOpenLog == null);

        MenuItem saveLogMenuItem = menu.findItem(R.id.menu_save_log);
        MenuItem saveAsLogMenuItem = menu.findItem(R.id.menu_save_as_log);

        saveLogMenuItem.setEnabled(showingMainLog);
        saveLogMenuItem.setVisible(showingMainLog);

        saveAsLogMenuItem.setEnabled(!showingMainLog);
        saveAsLogMenuItem.setVisible(!showingMainLog);

        boolean recordingInProgress = ServiceHelper.checkIfServiceIsRunning(getApplicationContext(), LogcatRecordingService.class);

        MenuItem recordMenuItem = menu.findItem(R.id.menu_record_log);

        recordMenuItem.setEnabled(!recordingInProgress);
        recordMenuItem.setVisible(!recordingInProgress);

        MenuItem crazyLoggerMenuItem = menu.findItem(R.id.menu_crazy_logger_service);
        crazyLoggerMenuItem.setEnabled(UtilLogger.DEBUG_MODE);
        crazyLoggerMenuItem.setVisible(UtilLogger.DEBUG_MODE);

        MenuItem partialSelectMenuItem = menu.findItem(R.id.menu_partial_select);
        partialSelectMenuItem.setEnabled(!partialSelectMode);
        partialSelectMenuItem.setVisible(!partialSelectMode);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //used to workaround issue where the search text is cleared on expanding the SearchView

        mSearchViewMenuItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) mSearchViewMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!mDynamicallyEnteringSearchText) {
                    log.d("filtering: %s", newText);
                    search(newText);
                    populateSuggestionsAdapter(newText);
                }
                mDynamicallyEnteringSearchText = false;
                return false;
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                List<String> suggestions = getSuggestionsForQuery(mSearchingString);
                searchView.setQuery(suggestions.get(position), true);
                return false;
            }
        });
        searchView.setSuggestionsAdapter(mSearchSuggestionsAdapter);
        if (mSearchingString != null && !mSearchingString.isEmpty()) {
            mDynamicallyEnteringSearchText = true;
            mSearchViewMenuItem.expandActionView();
            searchView.setQuery(mSearchingString, true);
            searchView.clearFocus();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_play_pause) {
            pauseOrUnpause(item);
            return true;
        } else if (i == R.id.menu_expand_all) {
            expandOrCollapseAll(true);
            return true;
        } else if (i == R.id.menu_clear) {
            if (mLogListAdapter != null) {
                mLogListAdapter.clear();
            }
            Snackbar.make(findViewById(android.R.id.content), R.string.log_cleared, Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startMainLog();
                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(this, R.color.accent))
                    .show();
            return true;
        } else if (i == R.id.menu_log_level) {
            showLogLevelDialog();
            return true;
        } else if (i == R.id.menu_open_log) {
            showOpenLogFileDialog();
            return true;
        } else if (i == R.id.menu_save_log || i == R.id.menu_save_as_log) {
            showSaveLogDialog();
            return true;
        } else if (i == R.id.menu_record_log) {
            showRecordLogDialog();
            return true;
        } else if (i == R.id.menu_send_log) {
            showSendLogDialog();
            return true;
        } else if (i == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (i == R.id.menu_delete_saved_log) {
            startDeleteSavedLogsDialog();
            return true;
        } else if (i == R.id.menu_settings) {
            startSettingsActivity();
            return true;
        } else if (i == R.id.menu_crazy_logger_service) {
            ServiceHelper.startOrStopCrazyLogger(this);
            return true;
        } else if (i == R.id.menu_partial_select) {
            startPartialSelectMode();
            return true;
        } else if (i == R.id.menu_filters) {
            showFiltersDialog();
            return true;
        }
        return false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item, LogLine logLine) {
        if (logLine != null) {
            switch (item.getItemId()) {
                case CONTEXT_MENU_COPY_ID:
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                    clipboard.setPrimaryClip(ClipData.newPlainText(null, logLine.getOriginalLine()));
                    Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                    return true;
                case CONTEXT_MENU_FILTER_ID:

                    if (logLine.getProcessId() == -1) {
                        // invalid line
                        return false;
                    }

                    showSearchByDialog(logLine);
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(final View itemView, final LogLine logLine) {
        if (partialSelectMode) {
            logLine.setHighlighted(true);
            partiallySelectedLogLines.add(logLine);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mLogListAdapter.notifyItemChanged(binding.list.getChildAdapterPosition(itemView));
                }
            });

            if (partiallySelectedLogLines.size() == 2) {
                // last line
                completePartialSelect();
            }
        } else {
            logLine.setExpanded(!logLine.isExpanded());
            mLogListAdapter.notifyItemChanged(binding.list.getChildAdapterPosition(itemView));
        }
    }

    private void showSearchByDialog(final LogLine logLine) {
        int tagColor = LogLineAdapterUtil.getOrCreateTagColor(this, logLine.getTag());

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.filter_choice)
                .iconRes(R.drawable.ic_search)
                .customView(R.layout.dialog_searchby, false)
                .show();

        LinearLayout customView = (LinearLayout) dialog.getCustomView();
        LinearLayout tag = (LinearLayout) customView.findViewById(R.id.dialog_searchby_tag_linear);
        LinearLayout pid = (LinearLayout) customView.findViewById(R.id.dialog_searchby_pid_linear);

        TextView tagText = (TextView) customView.findViewById(R.id.dialog_searchby_tag_text);
        TextView pidText = (TextView) customView.findViewById(R.id.dialog_searchby_pid_text);

        ColorScheme colorScheme = PreferenceHelper.getColorScheme(this);

        tagText.setText(logLine.getTag());
        pidText.setText(Integer.toString(logLine.getProcessId()));
        tagText.setTextColor(tagColor);
        pidText.setTextColor(colorScheme.getForegroundColor(this));

        int backgroundColor = colorScheme.getSpinnerColor(this);
        pidText.setBackgroundColor(backgroundColor);
        tagText.setBackgroundColor(backgroundColor);

        tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tagQuery = (logLine.getTag().contains(" "))
                        ? ('"' + logLine.getTag() + '"')
                        : logLine.getTag();
                setSearchText(SearchCriteria.TAG_KEYWORD + tagQuery);
                dialog.dismiss();
                //TODO: put the cursor at the end
                                /*searchEditText.setSelection(searchEditText.length());*/
            }
        });

        pid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchText(SearchCriteria.PID_KEYWORD + logLine.getProcessId());
                dialog.dismiss();
                //TODO: put the cursor at the end
                                /*searchEditText.setSelection(searchEditText.length());*/
            }
        });
    }

    private void showRecordLogDialog() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    SHOW_RECORD_LOG_REQUEST);
            return;
        }
        // start up the dialog-like activity
        String[] suggestions = ArrayUtil.toArray(new ArrayList<>(mSearchSuggestionsSet), String.class);

        Intent intent = new Intent(LogcatActivity.this, RecordLogDialogActivity.class);
        intent.putExtra(RecordLogDialogActivity.EXTRA_QUERY_SUGGESTIONS, suggestions);

        startActivity(intent);
    }

    private void showFiltersDialog() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("t", "Started thread");
                final List<FilterItem> filters = new ArrayList<>();

                CatlogDBHelper dbHelper = null;
                try {
                    dbHelper = new CatlogDBHelper(LogcatActivity.this);
                    filters.addAll(dbHelper.findFilterItems());
                } finally {
                    if (dbHelper != null) {
                        dbHelper.close();
                    }
                }

                Collections.sort(filters);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final FilterAdapter filterAdapter = new FilterAdapter(LogcatActivity.this, filters);
                        ListView view = new ListView(LogcatActivity.this);
                        view.setAdapter(filterAdapter);
                        view.setDivider(null);
                        view.setDividerHeight(0);
                        View footer = getLayoutInflater().inflate(R.layout.list_header_add_filter, view, false);
                        view.addFooterView(footer);

                        final MaterialDialog dialog = new MaterialDialog.Builder(LogcatActivity.this)
                                .title(R.string.title_filters)
                                .customView(view, false)
                                .negativeText(android.R.string.cancel).show();

                        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (position == parent.getCount() - 1) {
                                    showAddFilterDialog(filterAdapter);
                                } else {
                                    // load filter
                                    String text = filterAdapter.getItem(position).getText();
                                    setSearchText(text);
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void showAddFilterDialog(final FilterAdapter filterAdapter) {

        // show a popup to add a new filter text
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        final AutoCompleteTextView editText =
                (AutoCompleteTextView) inflater.inflate(R.layout.dialog_new_filter, null, false);

        // show suggestions as the user types
        List<String> suggestions = new ArrayList<>(mSearchSuggestionsSet);
        SortedFilterArrayAdapter<String> suggestionAdapter = new SortedFilterArrayAdapter<>(
                this, R.layout.list_item_dropdown, suggestions);
        editText.setAdapter(suggestionAdapter);

        final MaterialDialog alertDialog = new MaterialDialog.Builder(this)
                .title(R.string.add_filter)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        handleNewFilterText(editText.getText().toString(), filterAdapter);
                        dialog.dismiss();
                    }
                })
                .negativeText(android.R.string.cancel)
                .customView(editText, true)
                .build();

        // when 'Done' is clicked (i.e. enter button), do the same as when "OK" is clicked
        editText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // dismiss soft keyboard

                    handleNewFilterText(editText.getText().toString(), filterAdapter);

                    alertDialog.dismiss();
                    return true;
                }
                return false;
            }
        });

        alertDialog.show();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, 0);

    }

    protected void handleNewFilterText(String text, final FilterAdapter filterAdapter) {
        final String trimmed = text.trim();
        if (!TextUtils.isEmpty(trimmed)) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    CatlogDBHelper dbHelper = null;
                    FilterItem item = null;
                    try {
                        dbHelper = new CatlogDBHelper(LogcatActivity.this);
                        item = dbHelper.addFilter(trimmed);
                    } finally {
                        if (dbHelper != null) {
                            dbHelper.close();
                        }
                    }

                    final FilterItem finalItem = item;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (finalItem != null) { // null indicates duplicate
                                filterAdapter.add(finalItem);
                                filterAdapter.sort(FilterItem.DEFAULT_COMPARATOR);
                                filterAdapter.notifyDataSetChanged();

                                addToAutocompleteSuggestions(trimmed);
                            }
                        }
                    });

                }
            }).start();
        }
    }

    private void startPartialSelectMode() {

        boolean hideHelp = PreferenceHelper.getHidePartialSelectHelpPreference(this);

        if (hideHelp) {
            partialSelectMode = true;
            partiallySelectedLogLines.clear();
            Toast.makeText(this, R.string.toast_started_select_partial, Toast.LENGTH_SHORT).show();
        } else {

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View helpView = inflater.inflate(R.layout.dialog_partial_save_help, null);
            // don't show the scroll bar
            helpView.setVerticalScrollBarEnabled(false);
            helpView.setHorizontalScrollBarEnabled(false);
            final CheckBox checkBox = (CheckBox) helpView.findViewById(android.R.id.checkbox);

            new MaterialDialog.Builder(this)
                    .title(R.string.menu_title_partial_select)
                    .customView(helpView, true)
                    .negativeText(android.R.string.cancel)
                    .positiveText(android.R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            partialSelectMode = true;
                            partiallySelectedLogLines.clear();
                            Toast.makeText(LogcatActivity.this, R.string.toast_started_select_partial, Toast.LENGTH_SHORT).show();

                            if (checkBox.isChecked()) {
                                // hide this help dialog in the future
                                PreferenceHelper.setHidePartialSelectHelpPreference(LogcatActivity.this, true);
                            }
                        }
                    })
                    .show();
        }
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SETTINGS);
    }

    private void expandOrCollapseAll(boolean change) {

        mCollapsedMode = change != mCollapsedMode;

        int oldFirstVisibleItem = ((LinearLayoutManager) binding.list.getLayoutManager()).findFirstVisibleItemPosition();

        for (LogLine logLine : mLogListAdapter.getTrueValues()) {
            if (logLine != null) {
                logLine.setExpanded(!mCollapsedMode);
            }
        }

        mLogListAdapter.notifyDataSetChanged();

        // ensure that we either stay autoscrolling at the bottom of the list...

        if (mAutoscrollToBottom) {

            scrollToBottom();

            // ... or that whatever was the previous first visible item is still the current first
            // visible item after expanding/collapsing

        } else if (oldFirstVisibleItem != -1) {

            binding.list.scrollToPosition(oldFirstVisibleItem);
        }

        supportInvalidateOptionsMenu();
    }

    private void startDeleteSavedLogsDialog() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    DELETE_SAVED_LOG_REQUEST);
            return;
        }
        if (!SaveLogHelper.checkSdCard(this)) {
            return;
        }

        List<CharSequence> filenames = new ArrayList<CharSequence>(SaveLogHelper.getLogFilenames());

        if (filenames.isEmpty()) {
            Toast.makeText(this, R.string.no_saved_logs, Toast.LENGTH_SHORT).show();
            return;
        }

        final CharSequence[] filenameArray = ArrayUtil.toArray(filenames, CharSequence.class);

        final LogFileAdapter logFileAdapter = new LogFileAdapter(this, filenames, -1, true);

        @SuppressLint("InflateParams") LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_delete_logfiles, null);

        ListView view = (ListView) layout.findViewById(R.id.list);
        view.setAdapter(logFileAdapter);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title(R.string.manage_saved_logs)
                .customView(layout, false)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.delete_all)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        boolean[] allChecked = new boolean[logFileAdapter.getCount()];

                        for (int i = 0; i < allChecked.length; i++) {
                            allChecked[i] = true;
                        }
                        verifyDelete(filenameArray, allChecked, dialog);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        verifyDelete(filenameArray, logFileAdapter.getCheckedItems(), dialog);
                    }
                })
                .positiveText(R.string.delete);

        builder.show();

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                logFileAdapter.checkOrUncheck(position);
            }
        });
    }

    protected void verifyDelete(final CharSequence[] filenameArray,
                                final boolean[] checkedItems, final DialogInterface parentDialog) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        int deleteCount = 0;

        for (boolean checkedItem : checkedItems) {
            if (checkedItem) {
                deleteCount++;
            }
        }


        final int finalDeleteCount = deleteCount;

        if (finalDeleteCount > 0) {

            builder.setTitle(R.string.delete_saved_log)
                    .setCancelable(true)
                    .setMessage(getResources().getQuantityString(R.plurals.are_you_sure, finalDeleteCount))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // ok, delete

                            for (int i = 0; i < checkedItems.length; i++) {
                                if (checkedItems[i]) {
                                    SaveLogHelper.deleteLogIfExists(filenameArray[i].toString());
                                }
                            }

                            String toastText = getResources().getQuantityString(R.plurals.files_deleted, finalDeleteCount);
                            Toast.makeText(LogcatActivity.this, toastText, Toast.LENGTH_SHORT).show();

                            dialog.dismiss();
                            parentDialog.dismiss();

                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }


    }

    private void showSendLogDialog() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    SEND_LOG_ID_REQUEST);
            return;
        }

        String[] items = new String[]{(String) getText(R.string.as_attachment), (String) getText(R.string.as_text)};

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View includeDeviceInfoView = inflater.inflate(R.layout.dialog_send_log, null, false);
        final CheckBox includeDeviceInfoCheckBox = (CheckBox) includeDeviceInfoView.findViewById(android.R.id.checkbox);

        // allow user to choose whether or not to include device info in report, use preferences for persistence
        includeDeviceInfoCheckBox.setChecked(PreferenceHelper.getIncludeDeviceInfoPreference(this));
        includeDeviceInfoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceHelper.setIncludeDeviceInfoPreference(LogcatActivity.this, isChecked);
            }
        });

        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.send_log_title)
                .setView(includeDeviceInfoView)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendLogToTargetApp(which == 1, includeDeviceInfoCheckBox.isChecked());
                        dialog.dismiss();
                    }
                })
                .show();

    }

    protected void sendLogToTargetApp(final boolean asText, final boolean includeDeviceInfo) {

        if (!(mCurrentlyOpenLog == null && asText) && !SaveLogHelper.checkSdCard(this)) {
            // if asText is false, then we need to check to make sure we can access the sdcard
            return;
        }

        final Handler ui = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            private MaterialDialog mDialog;

            @Override
            public void run() {
                ui.post(new Runnable() {
                    @Override
                    public void run() {
                        if (asText || mCurrentlyOpenLog == null || includeDeviceInfo) {
                            MaterialDialog.Builder progressDialog = new MaterialDialog.Builder(LogcatActivity.this);
                            progressDialog.title(R.string.dialog_please_wait);
                            progressDialog.content(getString(R.string.dialog_compiling_log));
                            progressDialog.progress(true, 0);
                            mDialog = progressDialog.show();
                            mDialog.setCanceledOnTouchOutside(false);
                            mDialog.setCancelable(false);
                        }
                    }
                });
                final SendLogDetails sendLogDetails = getSendLogDetails(asText, includeDeviceInfo);
                ui.post(new Runnable() {
                    @Override
                    public void run() {
                        startChooser(LogcatActivity.this, sendLogDetails.getSubject(), sendLogDetails.getBody(),
                                sendLogDetails.getAttachmentType(), sendLogDetails.getAttachment());
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        if (asText && sendLogDetails.getBody().length() > 100000) {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.as_text_not_work), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();

    }

    @WorkerThread
    private SendLogDetails getSendLogDetails(boolean asText, boolean includeDeviceInfo) {
        SendLogDetails sendLogDetails = new SendLogDetails();
        StringBuilder body = new StringBuilder();

        List<File> files = new ArrayList<>();

        if (!asText) {
            if (mCurrentlyOpenLog != null) { // use saved log file
                files.add(SaveLogHelper.getFile(mCurrentlyOpenLog));
            } else { // create a temp file to hold the current, unsaved log
                File tempLogFile = SaveLogHelper.saveTemporaryFile(this,
                        SaveLogHelper.TEMP_LOG_FILENAME, null, getCurrentLogAsListOfStrings());
                files.add(tempLogFile);
            }
        }

        if (includeDeviceInfo) {
            // include device info
            String deviceInfo = BuildHelper.getBuildInformationAsString();
            if (asText) {
                // append to top of body
                body.append(deviceInfo).append('\n');
            } else {
                // or create as separate file called device.txt
                File tempFile = SaveLogHelper.saveTemporaryFile(this,
                        SaveLogHelper.TEMP_DEVICE_INFO_FILENAME, deviceInfo, null);
                files.add(tempFile);
            }
        }

        if (asText) {
            body.append(getCurrentLogAsCharSequence());
        }

        sendLogDetails.setBody(body.toString());
        sendLogDetails.setSubject(getString(R.string.subject_log_report));

        // either zip up multiple files or just attach the one file
        switch (files.size()) {
            case 0: // no attachments
                sendLogDetails.setAttachmentType(SendLogDetails.AttachmentType.None);
                break;
            case 1: // one plaintext file attachment
                sendLogDetails.setAttachmentType(SendLogDetails.AttachmentType.Text);
                sendLogDetails.setAttachment(files.get(0));
                break;
            default: // 2 files - need to zip them up
                File zipFile = SaveLogHelper.saveTemporaryZipFile(SaveLogHelper.TEMP_ZIP_FILENAME, files);
                File tmpDirectory = SaveLogHelper.getTempDirectory();
                for (File file : files) {
                    // delete original files
                    if (file.getParentFile().equals(tmpDirectory)) { // only delete temporary files
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
                sendLogDetails.setAttachmentType(SendLogDetails.AttachmentType.Zip);
                sendLogDetails.setAttachment(zipFile);
                break;
        }

        return sendLogDetails;
    }

    private List<CharSequence> getCurrentLogAsListOfStrings() {

        List<CharSequence> result = new ArrayList<>(mLogListAdapter.getItemCount());

        for (int i = 0; i < mLogListAdapter.getItemCount(); i++) {
            result.add(mLogListAdapter.getItem(i).getOriginalLine());
        }

        return result;
    }

    private CharSequence getCurrentLogAsCharSequence() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < mLogListAdapter.getItemCount(); i++) {
            stringBuilder.append(mLogListAdapter.getItem(i).getOriginalLine()).append('\n');
        }

        return stringBuilder;
    }

    private void showSaveLogDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    SAVE_LOG_REQUEST);
            return;
        }

        if (!SaveLogHelper.checkSdCard(this)) {
            return;
        }

        MaterialDialog.InputCallback onClickListener = new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog materialDialog, CharSequence charSequence) {
                if (DialogHelper.isInvalidFilename(charSequence)) {
                    Toast.makeText(LogcatActivity.this, R.string.enter_good_filename, Toast.LENGTH_SHORT).show();
                } else {
                    String filename = charSequence.toString();
                    saveLog(filename);
                }
            }
        };

        DialogHelper.showFilenameSuggestingDialog(this, null, onClickListener, R.string.save_log);
    }

    private void savePartialLog(final String filename, LogLine first, LogLine last) {

        final List<CharSequence> logLines = new ArrayList<>(mLogListAdapter.getItemCount());

        // filter based on first and last
        boolean started = false;
        boolean foundLast = false;
        for (int i = 0; i < mLogListAdapter.getItemCount(); i++) {
            LogLine logLine = mLogListAdapter.getItem(i);
            if (logLine == first) {
                started = true;
            }
            if (started) {
                logLines.add(logLine.getOriginalLine());
            }
            if (logLine == last) {
                foundLast = true;
                break;
            }
        }

        if (!foundLast || logLines.isEmpty()) {
            Toast.makeText(this, R.string.toast_invalid_selection, Toast.LENGTH_LONG).show();
            cancelPartialSelect();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                SaveLogHelper.deleteLogIfExists(filename);
                final boolean saved = SaveLogHelper.saveLog(logLines, filename);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (saved) {
                            Toast.makeText(getApplicationContext(), R.string.log_saved, Toast.LENGTH_SHORT).show();
                            openLogFile(filename);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.unable_to_save_log, Toast.LENGTH_LONG).show();
                        }
                        cancelPartialSelect();
                    }
                });
            }
        }).start();
    }

    private void saveLog(final String filename) {

        // do in background to avoid jankiness

        final List<CharSequence> logLines = getCurrentLogAsListOfStrings();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SaveLogHelper.deleteLogIfExists(filename);
                final boolean saved = SaveLogHelper.saveLog(logLines, filename);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (saved) {
                            Toast.makeText(getApplicationContext(), R.string.log_saved, Toast.LENGTH_SHORT).show();
                            openLogFile(filename);
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.unable_to_save_log, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();

    }

    private void showOpenLogFileDialog() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    OPEN_LOG_REQUEST);
            return;
        }
        if (!SaveLogHelper.checkSdCard(this)) {
            return;
        }

        final List<CharSequence> filenames = new ArrayList<CharSequence>(SaveLogHelper.getLogFilenames());

        if (filenames.isEmpty()) {
            Toast.makeText(this, R.string.no_saved_logs, Toast.LENGTH_SHORT).show();
            return;
        }

        int logToSelect = mCurrentlyOpenLog != null ? filenames.indexOf(mCurrentlyOpenLog) : -1;
        ArrayAdapter<CharSequence> logFileAdapter = new LogFileAdapter(this, filenames, logToSelect, false);

        ListView view = new ListView(this);
        view.setAdapter(logFileAdapter);
        view.setDivider(null);
        view.setDividerHeight(0);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title(R.string.open_log)
                .customView(view, false);

        final MaterialDialog dialog = builder.show();


        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                String filename = filenames.get(position).toString();
                openLogFile(filename);
            }
        });

    }

    private void openLogFile(final String filename) {

        // do in background to avoid jank

        final AsyncTask<Void, Void, List<LogLine>> openFileTask = new AsyncTask<Void, Void, List<LogLine>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                resetDisplayedLog(filename);

                showProgressBar();
            }

            @Override
            protected List<LogLine> doInBackground(Void... params) {

                // remove any lines at the beginning if necessary
                final int maxLines = PreferenceHelper.getDisplayLimitPreference(LogcatActivity.this);
                SavedLog savedLog = SaveLogHelper.openLog(filename, maxLines);
                List<String> lines = savedLog.getLogLines();
                List<LogLine> logLines = new ArrayList<>();
                for (String line : lines) {
                    logLines.add(LogLine.newLogLine(line, !mCollapsedMode));
                }

                // notify the user if the saved file was truncated
                if (savedLog.isTruncated()) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            String toastText = getResources().getQuantityString(R.plurals.toast_log_truncated, maxLines);
                            Toast.makeText(LogcatActivity.this, toastText, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return logLines;
            }

            @Override
            protected void onPostExecute(List<LogLine> logLines) {
                super.onPostExecute(logLines);
                hideProgressBar();

                for (LogLine logLine : logLines) {
                    mLogListAdapter.addWithFilter(logLine, "");
                    addToAutocompleteSuggestions(logLine);

                }

                // scroll to bottom
                scrollToBottom();
            }
        };

        // if the main log task is running, we can only run AFTER it's been canceled

        if (mTask != null) {
            mTask.setOnFinished(new Runnable() {

                @Override
                public void run() {
                    openFileTask.execute((Void) null);

                }
            });
            mTask.unpause();
            mTask.killReader();
            mTask = null;
        } else {
            // main log not running; just open in this thread
            openFileTask.execute((Void) null);
        }


    }

    void hideProgressBar() {
        binding.mainDarkProgressBar.setVisibility(View.GONE);
        binding.mainLightProgressBar.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        ColorScheme colorScheme = PreferenceHelper.getColorScheme(LogcatActivity.this);
        binding.mainDarkProgressBar.setVisibility(colorScheme.isUseLightProgressBar() ? View.GONE : View.VISIBLE);
        binding.mainLightProgressBar.setVisibility(colorScheme.isUseLightProgressBar() ? View.VISIBLE : View.GONE);
    }


    public void resetDisplayedLog(String filename) {
        mLogListAdapter.clear();
        mCurrentlyOpenLog = filename;
        mCollapsedMode = !PreferenceHelper.getExpandedByDefaultPreference(getApplicationContext());
        addFiltersToSuggestions(); // filters are what initial populate the suggestions
        updateUiForFilename();
        resetFilter();
    }

    private void updateUiForFilename() {
        boolean logFileMode = mCurrentlyOpenLog != null;

        //noinspection ConstantConditions
        getSupportActionBar().setSubtitle(logFileMode ? mCurrentlyOpenLog : "");
        getSupportActionBar().setDisplayHomeAsUpEnabled(logFileMode);
        supportInvalidateOptionsMenu();
    }

    private void resetFilter() {
        String defaultLogLevel = Character.toString(PreferenceHelper.getDefaultLogLevelPreference(this));
        CharSequence[] logLevels = getResources().getStringArray(R.array.log_levels_values);
        int logLevelLimit = ArrayUtil.indexOf(logLevels, defaultLogLevel);
        mLogListAdapter.setLogLevelLimit(logLevelLimit);
        logLevelChanged();
    }

    private void showLogLevelDialog() {
        String[] logLevels = getResources().getStringArray(R.array.log_levels);

        // put the word "default" after whatever the default log level is
        String defaultLogLevel = Character.toString(PreferenceHelper.getDefaultLogLevelPreference(this));
        int index = ArrayUtil.indexOf(getResources().getStringArray(R.array.log_levels_values), defaultLogLevel);

        logLevels[index] = logLevels[index] + " " + getString(R.string.default_in_parens);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.log_level)
                .setCancelable(true)
                .setSingleChoiceItems(logLevels, mLogListAdapter.getLogLevelLimit(), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLogListAdapter.setLogLevelLimit(which);
                        logLevelChanged();
                        dialog.dismiss();

                    }
                });

        builder.show();
    }

    private void setUpAdapter() {

        mLogListAdapter = new LogLineAdapter();
        mLogListAdapter.setClickListener(this);

        binding.list.setAdapter(mLogListAdapter);

        binding.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                // update what the first viewable item is
                final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                // if the bottom of the list isn't visible anymore, then stop autoscrolling
                mAutoscrollToBottom = (layoutManager.findLastCompletelyVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1);

                // only hide the fast scroll if we're unpaused and at the bottom of the list
                // TODO:
                //boolean enableFastScroll = mTask == null || mTask.isPaused() || !mAutoscrollToBottom;
                //mListView.setFastScrollEnabled(enableFastScroll);

            }
        });

        binding.list.setHasFixedSize(true);
    }

    private void completePartialSelect() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    COMPLETE_PARTIAL_SELECT_REQUEST);
            return;
        }
        if (!SaveLogHelper.checkSdCard(this)) {
            cancelPartialSelect();
            return;
        }

        MaterialDialog.InputCallback onClickListener = new MaterialDialog.InputCallback() {

            @Override
            public void onInput(@NonNull MaterialDialog materialDialog, CharSequence charSequence) {
                if (DialogHelper.isInvalidFilename(charSequence)) {
                    cancelPartialSelect();
                    Toast.makeText(LogcatActivity.this, R.string.enter_good_filename, Toast.LENGTH_SHORT).show();
                } else {
                    String filename = charSequence.toString();
                    savePartialLog(filename, partiallySelectedLogLines.get(0), partiallySelectedLogLines.get(1));
                }
            }
        };


        MaterialDialog.SingleButtonCallback onCancelListener = new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (which == DialogAction.NEGATIVE) {
                    cancelPartialSelect();
                }
            }
        };

        DialogHelper.showFilenameSuggestingDialog(this, onCancelListener, onClickListener, R.string.save_log);

    }

    private void cancelPartialSelect() {
        partialSelectMode = false;

        boolean changed = false;
        for (LogLine logLine : partiallySelectedLogLines) {
            if (logLine.isHighlighted()) {
                logLine.setHighlighted(false);
                changed = true;
            }
        }
        partiallySelectedLogLines.clear();
        if (changed) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mLogListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void setSearchText(String text) {
        // sets the search text without invoking autosuggestions, which are really only useful when typing
        mDynamicallyEnteringSearchText = true;
        search(text);
        supportInvalidateOptionsMenu();
    }

    private void search(String filterText) {
        Filter filter = mLogListAdapter.getFilter();
        filter.filter(filterText, this);
        mSearchingString = filterText;
    }

    private void pauseOrUnpause(MenuItem item) {
        LogReaderAsyncTask currentTask = mTask;

        if (currentTask != null) {
            if (currentTask.isPaused()) {
                currentTask.unpause();
                item.setIcon(R.drawable.ic_pause_white_24dp);
            } else {
                currentTask.pause();
                item.setIcon(R.drawable.ic_play_arrow_white_24dp);
            }
        }
    }


    @Override
    public void onFilterComplete(int count) {
        // always scroll to the bottom when searching
        binding.list.scrollToPosition(count - 1);

    }


    private void logLevelChanged() {
        search(mSearchingString);
    }

    private void updateBackgroundColor() {
        ColorScheme colorScheme = PreferenceHelper.getColorScheme(this);

        final int color = colorScheme.getBackgroundColor(LogcatActivity.this);

        mHandler.post(new Runnable() {
            public void run() {
                binding.mainBackground.setBackgroundColor(color);
            }
        });

        //TODO:
        //mListView.setCacheColorHint(color);
        //mListView.setDivider(new ColorDrawable(color));

    }


    private void addToAutocompleteSuggestions(LogLine logLine) {
        // add the tags to the autocompletetextview

        if (!StringUtil.isEmptyOrWhitespaceOnly(logLine.getTag())) {
            String trimmed = logLine.getTag().trim();
            addToAutocompleteSuggestions(trimmed);
        }
    }

    private void addToAutocompleteSuggestions(String trimmed) {
        if (mSearchSuggestionsSet.size() < MAX_NUM_SUGGESTIONS
                && !mSearchSuggestionsSet.contains(trimmed)) {
            mSearchSuggestionsSet.add(trimmed);
            populateSuggestionsAdapter(mSearchingString);
            //searchSuggestionsAdapter.add(trimmed);
        }
    }

    @SuppressLint("RestrictedApi")
    public void invalidateDarkOrLightMenuItems(Context context, Menu menu) {
        if (menu != null && menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
            /*final boolean darkMode = ThemeUtils.isDarkMode(context);
            final int textColorPrimary = Utils.resolveColor(context, android.R.attr.textColorPrimary);

            mToolbar.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < menu.size(); i++) {
                        MenuItemImpl item = (MenuItemImpl) menu.getItem(i);
                        int color = darkMode || item.isActionButton() ? Color.WHITE : textColorPrimary;
                        if (item.getIcon() != null) {
                            item.getIcon().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                        }
                    }
                }
            });*/
        }
    }

    private void scrollToBottom() {
        binding.list.scrollToPosition(mLogListAdapter.getItemCount() - 1);
    }

    private class LogReaderAsyncTask extends AsyncTask<Void, LogLine, Void> {

        private final Object mLock = new Object();
        private int counter = 0;
        private volatile boolean mPaused;
        private boolean mFirstLineReceived;
        private boolean mKilled;
        private LogcatReader mReader;
        private Runnable mOnFinishedRunnable;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            log.d("onPreExecute()");

            resetDisplayedLog(null);

            showProgressBar();
        }

        @Override
        protected Void doInBackground(Void... params) {
            log.d("doInBackground()");

            try {
                // use "recordingMode" because we want to load all the existing lines at once
                // for a performance boost
                LogcatReaderLoader loader = LogcatReaderLoader.create(LogcatActivity.this, true);
                mReader = loader.loadReader();

                int maxLines = PreferenceHelper.getDisplayLimitPreference(LogcatActivity.this);

                String line;
                LinkedList<LogLine> initialLines = new LinkedList<>();
                while ((line = mReader.readLine()) != null) {
                    if (mPaused) {
                        synchronized (mLock) {
                            if (mPaused) {
                                mLock.wait();
                            }
                        }
                    }
                    LogLine logLine = LogLine.newLogLine(line, !mCollapsedMode);
                    if (!mReader.readyToRecord()) {
                        // "ready to record" in this case means all the initial lines have been flushed from the reader
                        initialLines.add(logLine);
                        if (initialLines.size() > maxLines) {
                            initialLines.removeFirst();
                        }
                    } else if (!initialLines.isEmpty()) {
                        // flush all the initial lines we've loaded
                        initialLines.add(logLine);
                        publishProgress(ArrayUtil.toArray(initialLines, LogLine.class));
                        initialLines.clear();
                    } else {
                        // just proceed as normal
                        publishProgress(logLine);
                    }
                }
            } catch (InterruptedException e) {
                log.d(e, "expected error");
            } catch (Exception e) {
                log.d(e, "unexpected error");
            } finally {
                killReader();
                log.d("AsyncTask has died");
            }

            return null;
        }

        public void killReader() {
            if (!mKilled) {
                synchronized (mLock) {
                    if (!mKilled && mReader != null) {
                        mReader.killQuietly();
                        mKilled = true;
                    }
                }
            }

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            log.d("onPostExecute()");
            doWhenFinished();
        }

        @Override
        protected void onProgressUpdate(LogLine... values) {
            super.onProgressUpdate(values);

            if (!mFirstLineReceived) {
                mFirstLineReceived = true;
                hideProgressBar();
            }
            for (LogLine logLine : values) {
                mLogListAdapter.addWithFilter(logLine, mSearchingString);

                addToAutocompleteSuggestions(logLine);
            }

            // how many logs to keep in memory?  this avoids OutOfMemoryErrors
            int maxNumLogLines = PreferenceHelper.getDisplayLimitPreference(LogcatActivity.this);

            // check to see if the list needs to be truncated to avoid out of memory errors
            if (++counter % UPDATE_CHECK_INTERVAL == 0
                    && mLogListAdapter.getTrueValues().size() > maxNumLogLines) {
                int numItemsToRemove = mLogListAdapter.getTrueValues().size() - maxNumLogLines;
                mLogListAdapter.removeFirst(numItemsToRemove);
                log.d("truncating %d lines from log list to avoid out of memory errors", numItemsToRemove);
            }

            if (mAutoscrollToBottom) {
                scrollToBottom();
            }

        }

        private void doWhenFinished() {
            if (mPaused) {
                unpause();
            }
            if (mOnFinishedRunnable != null) {
                mOnFinishedRunnable.run();
            }
        }

        public void pause() {
            synchronized (mLock) {
                mPaused = true;
            }
        }

        public void unpause() {
            synchronized (mLock) {
                mPaused = false;
                mLock.notify();
            }
        }

        public boolean isPaused() {
            return mPaused;
        }

        public void setOnFinished(Runnable onFinished) {
            this.mOnFinishedRunnable = onFinished;
        }


    }
}