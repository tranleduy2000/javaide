package com.nolanlawson.logcat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nolanlawson.logcat.data.FilterQueryWithLevel;
import com.nolanlawson.logcat.helper.DialogHelper;
import com.nolanlawson.logcat.helper.PreferenceHelper;
import com.nolanlawson.logcat.helper.WidgetHelper;
import com.nolanlawson.logcat.util.Callback;

public class ShowRecordLogDialogActivity extends Activity {
	
	public static final String EXTRA_QUERY_SUGGESTIONS = "suggestions";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.record_log_dialog);
		
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		// grab the search suggestions, if any
		final List<String> suggestions = (getIntent() != null && getIntent().hasExtra(EXTRA_QUERY_SUGGESTIONS))
				? Arrays.asList(getIntent().getStringArrayExtra(EXTRA_QUERY_SUGGESTIONS))
				: Collections.<String>emptyList();
		
		// ask the user the save a file to record the log
		
		final EditText editText = DialogHelper.createEditTextForFilenameSuggestingDialog(this);
		
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.record_dialog_linear_layout);
		
		// insert the edit text below the prompt text
		linearLayout.addView(editText, 2);
		
		Button okButton = (Button) findViewById(android.R.id.button1);
		Button filterButton = (Button) findViewById(android.R.id.button2);
		Button cancelButton = (Button) findViewById(android.R.id.button3);
		
		
		String defaultLogLevel = Character.toString(PreferenceHelper.getDefaultLogLevelPreference(this));
		
		final StringBuilder queryFilterText = new StringBuilder();
		final StringBuilder logLevelText = new StringBuilder(defaultLogLevel);
		
		filterButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogHelper.showFilterDialogForRecording(ShowRecordLogDialogActivity.this, queryFilterText.toString(), 
						logLevelText.toString(), suggestions, 
						new Callback<FilterQueryWithLevel>() {

							@Override
							public void onCallback(FilterQueryWithLevel result) {
								queryFilterText.replace(0, queryFilterText.length(), result.getFilterQuery());
								logLevelText.replace(0, logLevelText.length(), result.getLogLevel());
							}
				});
				
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (DialogHelper.isInvalidFilename(editText.getText())) {
					
					Toast.makeText(ShowRecordLogDialogActivity.this, R.string.enter_good_filename, Toast.LENGTH_SHORT).show();
				} else {
					
					String filename = editText.getText().toString();
					
					Runnable runnable = new Runnable(){

						@Override
						public void run() {
							finish();
						}
					};
					
					DialogHelper.startRecordingWithProgressDialog(filename, 
							queryFilterText.toString(), logLevelText.toString(), runnable, ShowRecordLogDialogActivity.this);
				}
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
	}

	
	@Override
	public void onPause() {
		super.onPause();
		// update widgets when the dialog is complete
		
		WidgetHelper.updateWidgets(this);
		
	}
}
