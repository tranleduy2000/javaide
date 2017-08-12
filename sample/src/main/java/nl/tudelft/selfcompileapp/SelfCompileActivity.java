package nl.tudelft.selfcompileapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.InputStream;
import java.util.Arrays;

/**
 * Offers various changes to be made to the app by the user. Also the user can
 * install the (changed) app as an update or share it via nfc.
 *
 * @author Paul Brussee
 *
 */
public class SelfCompileActivity extends Activity {

	static final int REQ_APP_ICON = 1;
	static final int REQ_APP_INFO = 2;

	UserInputFragment userInput;
	TaskManagerFragment taskManager;

	View frmChange;
	Button btnMimicApp;
	ImageView btnAppIcon;
	EditText txtAppName;
	Spinner spnAppTheme;
	EditText txtAppPackage;
	TextView lblStatus;
	ProgressBar prbProgress;
	ProgressBar prbSpinner;
	Button btnReset;
	Button btnCancel;
	Button btnInstall;

	protected void updateGui(boolean enabled) {
		frmChange.setVisibility(!enabled ? View.GONE : View.VISIBLE);
		btnMimicApp.setEnabled(enabled);
		btnAppIcon.setEnabled(enabled);
		txtAppName.setEnabled(enabled);
		lblStatus.setVisibility(enabled ? View.GONE : View.VISIBLE);
		lblStatus.setText(taskManager.getStatus());
		prbProgress.setVisibility(enabled ? View.GONE : View.VISIBLE);
		prbProgress.setProgress(taskManager.getProgress());
		prbSpinner.setVisibility(enabled ? View.GONE : View.VISIBLE);
		btnReset.setVisibility(!enabled ? View.GONE : View.VISIBLE);
		btnReset.setEnabled(enabled);
		btnCancel.setVisibility(enabled ? View.GONE : View.VISIBLE);
		btnCancel.setEnabled(!enabled);
		btnInstall.setVisibility(!enabled ? View.GONE : View.VISIBLE);
		btnInstall.setEnabled(enabled);
	}

	//////////////////// ACTIVITY LIFECYCLE ////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initTaskManager();
		initUserInput();

		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
			initNfc();
		}

		// Reset on first run
		if (!S.dirProj.exists()) {
			btnReset(btnReset);
		}
	}

	private void initNfc() {
		NfcAdapter.getDefaultAdapter(this).setBeamPushUrisCallback(new NfcAdapter.CreateBeamUrisCallback() {

			public Uri[] createBeamUris(android.nfc.NfcEvent event) {

				return new Uri[] { Uri.fromFile(S.apkRedistributable) };
			}
		}, this);
	}

	//////////////////// RETAINED FRAGMENTS ////////////////////

	private void initTaskManager() {
		// Fetch saved progress status
		FragmentManager fm = getFragmentManager();
		taskManager = (TaskManagerFragment) fm.findFragmentByTag(TaskManagerFragment.class.getSimpleName());

		if (taskManager == null) {
			taskManager = new TaskManagerFragment();

			fm.beginTransaction().add(taskManager, TaskManagerFragment.class.getSimpleName()).commit();
		}

		S.mkDirs();
	}

	private void initUserInput() {
		// Fetch saved user input
		FragmentManager fm = getFragmentManager();
		userInput = (UserInputFragment) fm.findFragmentByTag(UserInputFragment.class.getSimpleName());

		if (userInput == null) {
			userInput = new UserInputFragment();

			fm.beginTransaction().add(userInput, UserInputFragment.class.getSimpleName()).commit();

			// Default name
			userInput.appName = getString(R.string.appName);

			// Default icon
			userInput.appIcon = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);

			// Default theme
			userInput.appTheme = "Theme." + getResources().getStringArray(R.array.appThemes)[0];

			// Default package
			userInput.appPackage = getApplicationContext().getPackageName();
		}

		// Restore previous user input
		try {
			setTheme(userInput.getAppThemeId());

		} catch (Exception e) {
			e.printStackTrace();
		}

		setContentView(R.layout.activity_self_compile);

		frmChange = findViewById(R.id.frmChange);
		btnMimicApp = findViewById(R.id.btnMimicApp);
		btnAppIcon = findViewById(R.id.btnAppIcon);
		txtAppName = findViewById(R.id.txtAppName);
		spnAppTheme = findViewById(R.id.spnAppTheme);
		txtAppPackage = findViewById(R.id.txtAppPackage);
		lblStatus = findViewById(R.id.lblStatus);
		prbProgress = findViewById(R.id.prbProgress);
		prbSpinner = findViewById(R.id.prbSpinner);
		btnReset = findViewById(R.id.btnReset);
		btnCancel = findViewById(R.id.btnCancel);
		btnInstall = findViewById(R.id.btnInstall);

		// Set current working state
		updateGui(taskManager.isIdle());

		// Restore previous user input
		txtAppName.setText(userInput.getAppName());
		btnAppIcon.setImageBitmap(userInput.getAppIcon(getApplicationContext()));
		spnAppTheme.setSelection(
				Arrays.asList(getResources().getStringArray(R.array.appThemes)).indexOf(userInput.getAppTheme()));
		txtAppPackage.setText(userInput.getAppPackage());

		// Handle app icon change
		/**
		 * @see onActivityResult
		 **/

		// Handle app name change
		txtAppName.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void afterTextChanged(Editable s) {
				userInput.setAppName(s.toString());
			}

		});

		// Handle app theme change
		/**
		 * @source http://stackoverflow.com/a/9375624
		 */
		spnAppTheme.post(new Runnable() {
			public void run() {
				spnAppTheme.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						userInput.setAppTheme("Theme." + getResources().getStringArray(R.array.appThemes)[position]);
						recreate();
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}

				});
			}
		});

		// Handle app package change
		txtAppPackage.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void afterTextChanged(Editable s) {
				userInput.setAppPackage(s.toString());
			}

		});

	}

	//////////////////// ON CLICK BUTTONS ////////////////////

	public void btnMimicApp(View btnMimicApp) {
		btnMimicApp.setEnabled(false);
		Intent pickApp = new Intent(this, PickAppActivity.class);
		startActivityForResult(pickApp, REQ_APP_INFO);
	}

	public void btnAppIcon(View btnAppIcon) {
		btnAppIcon.setEnabled(false);
		Intent pickIcon = new Intent(Intent.ACTION_PICK);
		pickIcon.setType("image/*");
		startActivityForResult(pickIcon, REQ_APP_ICON);
	}

	public void btnReset(View btnReset) {
		btnReset.setEnabled(false);
		taskManager.startClean(this, null);
	}

	public void btnCancel(View btnCancel) {
		btnCancel.setEnabled(false);
		taskManager.cancelTask(this, null);
	}

	public void btnInstall(View btnInstall) {
		btnInstall.setEnabled(false);
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.fromFile(S.apkRedistributable), "application/vnd.android.package-archive");
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		if (S.apkRedistributable.exists()) {
//			startActivity(i);
//		} else {
			taskManager.startBuild(this, i);
//		}
	}

	//////////////////// INTENT CALLBACKS ////////////////////

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
		super.onActivityResult(requestCode, resultCode, returnedIntent);
		switch (requestCode) {

		case REQ_APP_ICON:
			if (resultCode == RESULT_OK) {
				try {
					Uri uriImg = returnedIntent.getData();
					InputStream is = getContentResolver().openInputStream(uriImg);
					Bitmap b = BitmapFactory.decodeStream(is);
					Bitmap icon = Bitmap.createScaledBitmap(b, ModifyDrawables.XXHDPI_ICON_PIXELS,
							ModifyDrawables.XXHDPI_ICON_PIXELS, false);
					is.close();
					b.recycle();

					userInput.setAppIcon(icon);

					btnAppIcon.setImageBitmap(icon);

					btnAppIcon.setEnabled(true);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			break;

		case REQ_APP_INFO:
			if (resultCode == RESULT_OK) {
				try {
					String packageName = returnedIntent.getStringExtra("app");

					PackageManager packMgr = getPackageManager();
					ApplicationInfo info = packMgr.getApplicationInfo(packageName, 0);

					String name = packMgr.getApplicationLabel(info).toString();
					Bitmap icon = Util.drawableToBitmap(packMgr.getApplicationIcon(info));

					userInput.setAppName(name);
					userInput.setAppIcon(icon);

					txtAppName.setText(name);
					btnAppIcon.setImageBitmap(icon);

					btnMimicApp.setEnabled(true);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			break;
		}
	}
}

