/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.ide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.duy.ide.editor.code.MainActivity;
import com.duy.ide.javaide.sample.activities.SampleActivity;
import com.duy.ide.javaide.setting.CompilerSettingActivity;
import com.duy.ide.setting.SettingsActivity;
import com.duy.ide.utils.DonateUtils;
import com.duy.ide.utils.StoreUtil;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pluscubed.logcat.ui.LogcatActivity;

/**
 * Handler for menu click
 * Created by Duy on 03-Mar-17.
 */

public class MenuEditor {
    @NonNull
    private MainActivity activity;
    @Nullable
    private EditorControl listener;
    private Menu menu;
    private Builder builder;

    public MenuEditor(@NonNull MainActivity activity,
                      @Nullable EditorControl listener) {
        this.activity = activity;
        this.builder = activity;
        this.listener = listener;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        activity.getMenuInflater().inflate(R.menu.menu_tool, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (menuItem.isCheckable()) menuItem.setChecked(!menuItem.isChecked());
        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(activity);
        switch (id) {
            case R.id.action_setting:
                activity.startActivity(new Intent(activity, SettingsActivity.class));
                break;
            case R.id.action_find:
                activity.showDialogFind();
                break;
            case R.id.action_find_and_replace:
                if (listener != null) listener.findAndReplace();
                break;
            case R.id.action_run:
                builder.runProject();
                break;
            case R.id.action_save:
                if (listener != null) listener.saveCurrentFile();
                break;
            case R.id.action_goto_line:
                if (listener != null) listener.goToLine();
                break;
            case R.id.action_format:
                analytics.logEvent("action_format_code", new Bundle());
                if (listener != null) listener.formatCode();
                break;
            case R.id.action_report_bug: {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/tranleduy2000/javaide/issues"));
                activity.startActivity(intent);
                break;
            }
            case R.id.action_undo:
                analytics.logEvent("action_undo", new Bundle());
                if (listener != null) listener.undo();
                break;
            case R.id.action_redo:
                analytics.logEvent("action_redo", new Bundle());
                if (listener != null) listener.redo();
                break;
            case R.id.action_more_feature:
                activity.openDrawer(GravityCompat.END);

                break;

            case R.id.action_donate:
                DonateUtils.showDialogDonate(activity);
                break;
            case R.id.action_new_java_project:
                activity.showDialogCreateJavaProject();
                break;
            case R.id.action_new_android_project:
                activity.showDialogCreateAndroidProject();
                break;
            case R.id.action_new_file:
                if (listener != null) listener.createNewFile(null);
                break;
            case R.id.action_new_class:
                activity.showDialogCreateNewClass(null);
                break;
            case R.id.action_edit_run:
                activity.showDialogRunConfig();
                break;
            case R.id.action_open_project:
                activity.showDialogOpenJavaProject();
                break;
            case R.id.action_open_android_project:
                activity.showDialogOpenAndroidProject();
                break;
            case R.id.action_build_jar:
                activity.buildJar();
                break;
            case R.id.action_build_apk:
                activity.buildApk();
                break;
            case R.id.action_sample:
                activity.startActivityForResult(new Intent(activity, SampleActivity.class),
                        MainActivity.REQUEST_CODE_SAMPLE);
                break;
            case R.id.action_see_logcat:
                activity.startActivity(new Intent(activity, LogcatActivity.class));
                break;
            case R.id.action_create_keystore:
                activity.createKeyStore();
                break;
            case R.id.action_install_cpp_nide:
                StoreUtil.gotoPlayStore(activity, "com.duy.c.cpp.compiler");
                break;

            case R.id.action_compiler_setting:
                activity.startActivity(new Intent(activity, CompilerSettingActivity.class));
                break;
        }
        return true;
    }


    @Nullable
    public EditorControl getListener() {
        return listener;
    }

    public void setListener(@Nullable EditorControl listener) {
        this.listener = listener;
    }

    public boolean getChecked(int action_auto_save) {
        if (menu != null) {
            if (menu.findItem(action_auto_save).isChecked()) {
                return true;
            }
        }
        return false;
    }

}
