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

package com.duy.editor;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.duy.editor.editor.EditorActivity;
import com.duy.editor.info.InfoActivity;
import com.duy.editor.setting.JavaPreferences;
import com.duy.editor.setting.SettingsActivity;
import com.duy.editor.system.InstallActivity;
import com.duy.editor.utils.DonateUtils;
import com.duy.editor.utils.StoreUtil;
import com.spartacusrex.spartacuside.TerminalPreferences;

/**
 * Handler for menu click
 * Created by Duy on 03-Mar-17.
 */

public class MenuEditor {
    @NonNull
    private EditorActivity activity;
    @Nullable
    private EditorControl listener;
    private Menu menu;
    private JavaPreferences pascalPreferences;

    public MenuEditor(@NonNull EditorActivity activity, @Nullable EditorControl listener) {
        this.activity = activity;
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
        menu.findItem(R.id.action_ime).setChecked(pascalPreferences.useImeKeyboard());

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (menuItem.isCheckable()) menuItem.setChecked(!menuItem.isChecked());
        switch (id) {
            case R.id.action_setting:
                activity.startActivity(new Intent(activity, SettingsActivity.class));
                break;
            case R.id.action_find:
                activity.showDialogFind();

                break;
            case R.id.action_find_and_replace:
                if (listener != null) {
                    listener.findAndReplace();
                }

//            case R.id.action_open_file:
//                listener.chooseFile(null);
//                break;
                break;
//            case R.id.action_doc:
//                if (listener != null) {
//                    listener.showDocumentActivity();
//                }
//                break;

//            case R.id.action_code_sample:
//                activity.startActivity(new Intent(activity, CodeSampleActivity.class));
//                break;
            case R.id.action_rate:
                StoreUtil.gotoPlayStore(activity, BuildConfig.APPLICATION_ID);
                break;
            case R.id.action_more_app:
                StoreUtil.moreApp(activity);
                break;
            case R.id.nav_run:
                if (listener != null) {
                    listener.runProgram();
                }
                break;
//            case R.id.action_compile:
//                if (listener != null) {
//                    listener.doCompile();
//
//                break;
            case R.id.action_save:
                if (listener != null) {
                    listener.saveFile();
                }

                break;
            case R.id.action_save_as:
                if (listener != null) {
                    listener.saveAs();
                }

                break;
            case R.id.action_goto_line:
                if (listener != null) {
                    listener.goToLine();
                }

                break;
            case R.id.action_format:
                if (listener != null) {
                    listener.formatCode();
                }
                break;
            case R.id.action_report_bug:
                if (listener != null) {
                    listener.reportBug();
                }
                break;

            case R.id.action_undo:
                if (listener != null) {
                    listener.undo();
                }

                break;
            case R.id.action_redo:
                if (listener != null) {
                    listener.redo();
                }

                break;
            case R.id.action_paste:
                if (listener != null) {
                    listener.paste();
                }

                break;
            case R.id.action_copy_all:
                if (listener != null) {
                    listener.copyAll();
                }

                break;
            case R.id.action_select_theme:
                if (listener != null) {
                    listener.selectThemeFont();
                }
                break;
            case R.id.action_more_feature:
                activity.openDrawer(GravityCompat.END);

                break;
//            case R.id.action_translate:
//                showPopupTranslate(activity);
//
//                break;
            case R.id.action_info:
                activity.startActivity(new Intent(activity, InfoActivity.class));
                break;
//            case R.id.action_program_structure:
//                activity.showProgramStructure();
//
//                break;
//            case R.id.action_debug:
//                activity.startDebug();
//
//                break;
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
            case R.id.action_open_file:
                activity.openDrawer(GravityCompat.START);

                break;
            case R.id.action_insert_media_url:
                Intent i = new Intent();
                i.setType("audio/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                activity.startActivityForResult(Intent.createChooser(i, "Complete action using"),
                        EditorActivity.ACTION_PICK_MEDIA_URL);

                break;
            case R.id.action_insert_color:
                activity.insertColor();
                break;
            case R.id.action_ime:
                pascalPreferences.setImeMode(menuItem.isChecked());
                break;
            case R.id.action_donate:
                DonateUtils.showDialogDonate(activity);
                break;
            case R.id.action_setting_console:
                activity.startActivity(new Intent(activity, TerminalPreferences.class));
                break;

            case R.id.action_new_project:
                activity.showDialogCreateProject();
                break;
            case R.id.action_new_file:
                if (listener != null) listener.createNewFile(null);
                break;
            case R.id.action_new_class:
                activity.showDialogCreateClass();
                break;
            case R.id.action_install:
                activity.startActivity(new Intent(activity, InstallActivity.class));
                break;
        }
        return true;
    }


    public void showPopupTranslate(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.help_me_translate).setView(R.layout.dialog_help_translate)
                .setIcon(R.drawable.ic_language_white_24dp);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        final EditText editTitle = (EditText) alertDialog.findViewById(R.id.edit_title);
        final EditText editContent = (EditText) alertDialog.findViewById(R.id.edit_content);
        final Button btnSend = (Button) alertDialog.findViewById(R.id.btn_email);
        assert btnSend != null;
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"tranleduy1233@gmail.com"});
                assert editTitle != null;
                assert editContent != null;
                String content = "Help translate: \n" + editTitle.getText().toString() + "\n"
                        + editContent.getText().toString();
                i.putExtra(Intent.EXTRA_TEXT, content);
                try {
                    activity.startActivity(Intent.createChooser(i, activity.getString(R.string.send_mail)));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(activity, R.string.no_mail_clients, Toast.LENGTH_SHORT).show();
                }
                alertDialog.cancel();
            }
        });

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
