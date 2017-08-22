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

package com.duy.ide.themefont.themes;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.ide.editor.code.view.EditorView;
import com.duy.ide.file.FileManager;
import com.duy.ide.setting.AppSetting;
import com.duy.ide.themefont.themes.database.CodeTheme;
import com.duy.ide.themefont.themes.database.ThemeDatabase;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.IOException;

/**
 * Created by Duy on 12-Jul-17.
 */

public class CustomThemeActivity extends AbstractAppCompatActivity implements View.OnClickListener {
    private EditorView mEditorView;
    private CodeTheme codeTheme;
    private ThemeDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        codeTheme = ThemeManager.getDefault(this);
        mDatabase = new ThemeDatabase(this);

        setContentView(R.layout.acitivty_custom_theme);
        super.setupToolbar();
        setTitle(getString(R.string.custom_theme));
        bindView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_theme, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                showDialogSave();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialogSave() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_save_theme);
        builder.setTitle(R.string.custom_theme);
        final AlertDialog alertDialog = builder.create();

        alertDialog.show();
        final EditText editText = (EditText) alertDialog.findViewById(R.id.edit_name);
        View save = alertDialog.findViewById(R.id.btn_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().isEmpty()) {
                    editText.setError(getString(R.string.enter_new_file_name));
                } else {
                    String name = editText.getText().toString().trim();
                    if (mDatabase.hasValue(name)) {
                        editText.setError("Theme has been exits!");
                    } else {
                        codeTheme.setName(name);
                        mDatabase.insert(codeTheme);
                        alertDialog.cancel();
                        finish();
                    }
                }
            }
        });
        View apply = alertDialog.findViewById(R.id.btn_apply);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().isEmpty()) {
                    editText.setError(getString(R.string.enter_new_file_name));
                } else {
                    String name = editText.getText().toString();
                    if (mDatabase.hasValue(name)) {
                        editText.setError("Theme has been exits!");
                    } else {
                        codeTheme.setName(name);
                        mDatabase.insert(codeTheme);
                        AppSetting pascalPreferences = new AppSetting(CustomThemeActivity.this);
                        pascalPreferences.setTheme(name);
                        alertDialog.cancel();
                        finish();
                    }

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.close();
    }

    private void bindView() {
        mEditorView = (EditorView) findViewById(R.id.editor_view);
        mEditorView.setCodeTheme(codeTheme);
        try {
            mEditorView.setText(FileManager.streamToString(getAssets().open("source/preview.pas")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        findViewById(R.id.color_background).setOnClickListener(this);
        findViewById(R.id.color_normal).setOnClickListener(this);
        findViewById(R.id.color_keyword).setOnClickListener(this);
        findViewById(R.id.color_number).setOnClickListener(this);
        findViewById(R.id.color_string).setOnClickListener(this);
        findViewById(R.id.color_comment).setOnClickListener(this);
        findViewById(R.id.color_error).setOnClickListener(this);
        findViewById(R.id.color_opt).setOnClickListener(this);

        findViewById(R.id.color_background).setBackgroundColor(codeTheme.getBackground());
        findViewById(R.id.color_normal).setBackgroundColor(codeTheme.getTextColor());
        findViewById(R.id.color_keyword).setBackgroundColor(codeTheme.getKeywordColor());
        findViewById(R.id.color_number).setBackgroundColor(codeTheme.getNumberColor());
        findViewById(R.id.color_string).setBackgroundColor(codeTheme.getStringColor());
        findViewById(R.id.color_comment).setBackgroundColor(codeTheme.getCommentColor());
        findViewById(R.id.color_error).setBackgroundColor(codeTheme.getErrorColor());
        findViewById(R.id.color_opt).setBackgroundColor(codeTheme.getOptColor());
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.color_background:
            case R.id.color_normal:
            case R.id.color_keyword:
            case R.id.color_number:
            case R.id.color_string:
            case R.id.color_comment:
            case R.id.color_error:
            case R.id.color_opt:
                setColorForView(id);
                break;
        }
    }

    private void setColorForView(@IdRes final int id) {
        final View view = findViewById(id);
        int color = Color.WHITE;
        Drawable background = view.getBackground();
        if (background instanceof ColorDrawable)
            color = ((ColorDrawable) background).getColor();

        ColorPickerDialogBuilder.with(this)
                .showAlphaSlider(false)
                .initialColor(color)
                .setPositiveButton(android.R.string.ok,
                        new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                                view.setBackgroundColor(lastSelectedColor);
                                setColor(id, lastSelectedColor);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).build().show();
    }

    private void setColor(int id, int color) {
        switch (id) {
            case R.id.color_background:
                codeTheme.putColor("background_color", color);
                break;
            case R.id.color_normal:
                codeTheme.putColor("normal_text_color", color);
                break;
            case R.id.color_keyword:
                codeTheme.putColor("key_word_color", color);
                break;
            case R.id.color_number:
                codeTheme.putColor("number_color", color);
                break;
            case R.id.color_string:
                codeTheme.putColor("string_color", color);
                break;
            case R.id.color_comment:
                codeTheme.putColor("comment_color", color);
                break;
            case R.id.color_error:
                codeTheme.putColor("error_color", color);
                break;
            case R.id.color_opt:
                codeTheme.putColor("opt_color", color);
                break;
        }
        mEditorView.setCodeTheme(codeTheme);
    }


}
