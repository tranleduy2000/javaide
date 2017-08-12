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

import com.duy.ide.activities.InstallActivity;
import com.duy.ide.code_sample.activities.SampleActivity;
import com.duy.ide.editor.MainActivity;
import com.duy.ide.setting.JavaPreferences;
import com.duy.ide.setting.SettingsActivity;
import com.duy.ide.utils.DonateUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

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
    private JavaPreferences pascalPreferences;
    private Builder builder;

    public MenuEditor(@NonNull MainActivity activity,
                      @Nullable EditorControl listener) {
        this.activity = activity;
        this.builder = activity;
        this.listener = listener;
        pascalPreferences = new JavaPreferences(this.activity);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        activity.getMenuInflater().inflate(R.menu.menu_tool, menu);

        /*
         * set state for menu editor
         */
        menu.findItem(R.id.action_show_line).setChecked(pascalPreferences.isShowLines());
        menu.findItem(R.id.action_show_symbol).setChecked(pascalPreferences.isShowListSymbol());
        menu.findItem(R.id.action_show_popup).setChecked(pascalPreferences.isShowSuggestPopup());
        menu.findItem(R.id.action_edit_word_wrap).setChecked(pascalPreferences.isWrapText());
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
            case R.id.action_github: {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/tranleduy2000/javaide"));
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
            case R.id.action_paste:
                if (listener != null) listener.paste();
                break;
            case R.id.action_copy_all:
                if (listener != null) listener.copyAll();
                break;
            case R.id.action_select_theme:
                analytics.logEvent("action_select_theme", new Bundle());
                if (listener != null) listener.selectThemeFont();
                break;
            case R.id.action_more_feature:
                activity.openDrawer(GravityCompat.END);

                break;
            case R.id.action_show_line:
                pascalPreferences.setShowLines(menuItem.isChecked());
                break;
            case R.id.action_show_popup:
                pascalPreferences.setShowSuggestPopup(menuItem.isChecked());
                break;
            case R.id.action_show_symbol:
                pascalPreferences.setShowSymbol(menuItem.isChecked());

                break;
            case R.id.action_edit_word_wrap:
                pascalPreferences.setWordWrap(menuItem.isChecked());
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
                activity.showDialogSelectFileType(null);
                break;
            case R.id.action_install:
                activity.startActivity(new Intent(activity, InstallActivity.class));
                break;

            case R.id.action_edit_run:
                activity.showDialogRunConfig();
                break;
            case R.id.action_open_project:
                activity.showDialogOpenProject();
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
