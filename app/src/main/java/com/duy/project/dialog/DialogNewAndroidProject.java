package com.duy.project.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.sdklib.xml.AndroidManifestParser;
import com.android.sdklib.xml.ManifestData;
import com.duy.ide.R;
import com.duy.ide.code_sample.model.AssetUtil;
import com.duy.ide.file.FileManager;
import com.duy.project.file.android.AndroidProjectFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by Duy on 16-Jul-17.
 */

public class DialogNewAndroidProject extends AppCompatDialogFragment implements View.OnClickListener {
    public static final String TAG = "DialogNewAndroidProject";
    private EditText editProjectName, editPackage;
    private Button btnCreate, btnCancel;
    @Nullable
    private DialogNewJavaProject.OnCreateProjectListener listener;
    private EditText activityName, layoutName;

    public static DialogNewAndroidProject newInstance() {

        Bundle args = new Bundle();

        DialogNewAndroidProject fragment = new DialogNewAndroidProject();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DialogNewJavaProject.OnCreateProjectListener) getActivity();
        } catch (Exception e) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_new_android_project, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editPackage = view.findViewById(R.id.edit_package_name);
        editProjectName = view.findViewById(R.id.edit_project_name);
        btnCreate = view.findViewById(R.id.btn_create);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCreate.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        activityName = view.findViewById(R.id.edit_activity_name);
        layoutName = view.findViewById(R.id.edit_layout_name);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_create:
                doCreateProject();
                break;
        }
    }

    private void doCreateProject() {
        if (isOk()) {
            ///create new android project
            String packageName = editPackage.getText().toString();
            String activityName = this.activityName.getText().toString();
            String activityClass = packageName + "." + activityName;
            String mainLayoutName = layoutName.getText().toString();
            String appName = editProjectName.getText().toString();
            String classpath = FileManager.getClasspathFile(getContext()).getPath();

            String projectName = appName.replaceAll("\\s+", "");
            AndroidProjectFolder projectFile = new AndroidProjectFolder(
                    new File(FileManager.EXTERNAL_DIR), activityClass, packageName, projectName, classpath);
            try {
                //create directory
                projectFile.mkdirs();
                //copy resource
                AssetManager assets = getContext().getAssets();
                AssetUtil.copyAssetFolder(assets,
                        "templates/src/main/res", projectFile.getDirRes().getPath());

                //modified strings.xml
                File stringxml = new File(projectFile.getDirRes(), "values/strings.xml");
                String strings = FileManager.streamToString(new FileInputStream(
                        stringxml)).toString();
                strings = strings.replace("{APP_NAME}", appName);
                strings = strings.replace("{MAIN_ACTIVITY_NAME}", appName);
                Log.d(TAG, "doCreateProject strings = " + strings);
                FileManager.saveFile(stringxml, strings);

                File manifest = projectFile.getXmlManifest();
                InputStream manifestTemplate = assets.open("templates/src/main/AndroidManifest.xml");
                String contentManifest = FileManager.streamToString(manifestTemplate).toString();
                contentManifest = contentManifest.replace("{PACKAGE}", packageName);
//                contentManifest = contentManifest.replace("{APP_NAME}", appName);
                contentManifest = contentManifest.replace("{MAIN_ACTIVITY}", activityClass);
                Log.d(TAG, "doCreateProject contentManifest = " + contentManifest);
                FileManager.saveFile(manifest, contentManifest);

                //main activity
                File activityFile = FileManager.createFileIfNeed(new File(projectFile.dirJava,
                        activityClass.replace(".", File.separator) + ".java"));
                InputStream activityTemplate = assets.open("templates/src/main/MainActivity.java");
                String contentClass = FileManager.streamToString(activityTemplate).toString();
                contentClass = contentClass.replace("{PACKAGE}", packageName);
                contentClass = contentClass.replace("{APP_NAME}", appName);
                contentClass = contentClass.replace("{ACTIVITY_NAME}", activityName);
                Log.d(TAG, "doCreateProject contentManifest = " + contentClass);
                FileManager.saveFile(activityFile, contentClass);

                if (!mainLayoutName.contains(".")) mainLayoutName += ".xml";
                File layoutMain = new File(projectFile.getDirLayout(), mainLayoutName);
                layoutMain.createNewFile();
                InputStream layoutTemplate = assets.open("templates/src/main/activity_main.xml");
                String contentLayout = FileManager.streamToString(layoutTemplate).toString();
                FileManager.saveFile(layoutMain, contentLayout);

                //copy keystore
                File file = projectFile.getKeyStore().getFile();
                FileOutputStream out = new FileOutputStream(file);
                FileManager.copyFile(assets.open("templates/src/main/androiddebug.jks"), out);
                out.close();

                //copy android support library
                AssetUtil.copyAssetFolder(assets, "templates/libs", projectFile.dirLibs.getPath());


                if (listener != null) {
                    listener.onProjectCreated(projectFile);
                }
                this.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Can not create project. Error " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * check input data
     *
     * @return true if all is ok
     */
    private boolean isOk() {
        if (editProjectName.getText().toString().isEmpty()) {
            editProjectName.setError(getString(R.string.enter_name));
            return false;
        }
        if (editPackage.getText().toString().isEmpty()) {
            editPackage.setError(getString(R.string.enter_package));
            return false;
        }

        if (activityName.getText().toString().isEmpty()) {
            activityName.setError(getString(R.string.enter_name));
            return false;
        }
        if (layoutName.getText().toString().isEmpty()) {
            layoutName.setError(getString(R.string.enter_name));
            return false;
        }
        return true;
    }

}
