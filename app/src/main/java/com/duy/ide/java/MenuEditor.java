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

package com.duy.ide.java;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import com.duy.ide.R;
import com.duy.ide.java.editor.code.JavaIdeActivity;
import com.duy.ide.java.setting.SettingsActivity;
import com.duy.ide.java.utils.DonateUtils;
import com.duy.ide.java.utils.StoreUtil;
import com.duy.ide.javaide.sample.activities.JavaSampleActivity;
import com.duy.ide.javaide.setting.CompilerSettingActivity;
import com.pluscubed.logcat.ui.LogcatActivity;

/**
 * Handler for menu click
 * Created by Duy on 03-Mar-17.
 */

public class MenuEditor {
    @NonNull
    private JavaIdeActivity activity;
    @Nullable
    private EditorControl listener;
    private Menu menu;
    private Builder builder;

    public MenuEditor(@NonNull JavaIdeActivity activity,
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
        switch (id) {
            case R.id.action_setting:
                activity.startActivity(new Intent(activity, SettingsActivity.class));
                break;
            case R.id.action_run:
                builder.runProject();
                break;
            case R.id.action_report_bug: {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/tranleduy2000/javaide/issues"));
                activity.startActivity(intent);
                break;
            }
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
            case R.id.action_open_project:
                activity.showDialogOpenJavaProject();
                break;
            case R.id.action_open_android_project:
                activity.showDialogOpenAndroidProject();
                break;
            case R.id.action_sample:
                activity.startActivityForResult(new Intent(activity, JavaSampleActivity.class),
                        JavaIdeActivity.REQUEST_CODE_SAMPLE);
                break;
            case R.id.action_see_logcat:
                activity.startActivity(new Intent(activity, LogcatActivity.class));
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
