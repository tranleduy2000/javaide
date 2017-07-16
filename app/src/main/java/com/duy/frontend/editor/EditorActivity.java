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

package com.duy.frontend.editor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.frontend.MenuEditor;
import com.duy.frontend.R;
import com.duy.frontend.code.CompileManager;
import com.duy.frontend.code_sample.activities.DocumentActivity;
import com.duy.frontend.dialog.DialogCreateNewFile;
import com.duy.frontend.dialog.DialogManager;
import com.duy.frontend.editor.view.AutoIndentEditText;
import com.duy.frontend.editor.view.EditorView;
import com.duy.frontend.editor.view.adapters.InfoItem;
import com.duy.frontend.setting.PascalPreferences;
import com.duy.frontend.themefont.activities.ThemeFontActivity;
import com.duy.frontend.utils.DonateUtils;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.File;
import java.util.ArrayList;

public class EditorActivity extends BaseEditorActivity implements
        DrawerLayout.DrawerListener {

    public static final int ACTION_FILE_SELECT_CODE = 1012;
    public static final int ACTION_PICK_MEDIA_URL = 1013;
    public static final int ACTION_CREATE_SHORTCUT = 1014;

    private CompileManager mCompileManager;
    private MenuEditor menuEditor;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCompileManager = new CompileManager(this);
        mDrawerLayout.addDrawerListener(this);

        menuEditor = new MenuEditor(this, this);
        if (DonateUtils.DONATED) {
            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.action_donate).setVisible(false);
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                return menuEditor.onOptionsItemSelected(item);
            }
        });
        findViewById(R.id.img_tab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertTab(v);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return menuEditor.onOptionsItemSelected(item);
    }


    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    void insertTab(View v) {
        onKeyClick(v, AutoIndentEditText.TAB_CHARACTER);
    }

    @Override
    public void onKeyClick(View view, String text) {
        EditorFragment currentFragment = pagerAdapter.getCurrentFragment();
        if (currentFragment != null) {
            currentFragment.insert(text);
        }
    }

    @Override
    public void onKeyLongClick(String text) {
        EditorFragment currentFragment = pagerAdapter.getCurrentFragment();
        if (currentFragment != null) {
            currentFragment.insert(text);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return menuEditor.onCreateOptionsMenu(menu);
    }

    /**
     * create dialog find and replace
     */
    @Override
    public void findAndReplace() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setView(R.layout.dialog_find_and_replace);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        final CheckBox ckbRegex = (CheckBox) alertDialog.findViewById(R.id.ckb_regex);
        final CheckBox ckbMatch = (CheckBox) alertDialog.findViewById(R.id.ckb_match_key);
        final EditText editFind = (EditText) alertDialog.findViewById(R.id.txt_find);
        final EditText editReplace = (EditText) alertDialog.findViewById(R.id.edit_replace);
        editFind.setText(getPreferences().getString(PascalPreferences.LAST_FIND));
        alertDialog.findViewById(R.id.btn_replace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
                if (editorFragment != null) {
                    editorFragment.doFindAndReplace(
                            editFind.getText().toString(),
                            editReplace.getText().toString(),
                            ckbRegex.isChecked(),
                            ckbMatch.isChecked());
                }
                getPreferences().put(PascalPreferences.LAST_FIND, editFind.getText().toString());
                alertDialog.dismiss();
            }
        });
        alertDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


    }

    @Override
    public void runProgram() {
        if (doCompile()) mCompileManager.execute(getCurrentFilePath());
    }

    @Override
    public boolean isAutoSave() {
        return menuEditor.getChecked(R.id.action_auto_save);
    }

    /**
     * replace dialog find
     */
    public void showDialogFind() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setView(R.layout.dialog_find);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        final CheckBox ckbRegex = (CheckBox) alertDialog.findViewById(R.id.ckb_regex);
        final CheckBox ckbMatch = (CheckBox) alertDialog.findViewById(R.id.ckb_match_key);
        final CheckBox ckbWordOnly = (CheckBox) alertDialog.findViewById(R.id.ckb_word_only);
        final EditText editFind = (EditText) alertDialog.findViewById(R.id.txt_find);
        editFind.setText(getPreferences().getString(PascalPreferences.LAST_FIND));
        alertDialog.findViewById(R.id.btn_replace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
                if (editorFragment != null) {
                    editorFragment.doFind(editFind.getText().toString(),
                            ckbRegex.isChecked(),
                            ckbWordOnly.isChecked(),
                            ckbMatch.isChecked());
                }
                getPreferences().put(PascalPreferences.LAST_FIND, editFind.getText().toString());
                alertDialog.dismiss();
            }
        });
        alertDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


    }

    @Override
    public void saveFile() {
        EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.saveFile();
        }
    }

    @Override
    public void showDocumentActivity() {
        Intent intent = new Intent(this, DocumentActivity.class);
        startActivity(intent);
    }


    public String getCode() {
        EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
        if (editorFragment != null) {
            return editorFragment.getCode();
        }
        return "";
    }

    /**
     * compile code, if is error, show dialog error
     * invalidate keyword
     */
    @Override
    public boolean doCompile() {
        saveFile();
        String filePath = getCurrentFilePath();
        if (filePath.isEmpty()) return false;
        try {
//            CodeUnit codeUnit;
//            if (getCode().trim().toLowerCase().startsWith("unit ")) {
//
//                ArrayList<ScriptSource> searchPath = new ArrayList<>();
//                searchPath.add(new FileScriptSource(new File(filePath).getParent()));
//                codeUnit = PascalCompiler.loadLibrary(new File(filePath).getName(),
//                        new FileReader(filePath),
//                        searchPath,
//                        new ProgramHandler(filePath));
//            } else {
//
//                ArrayList<ScriptSource> searchPath = new ArrayList<>();
//                searchPath.add(new FileScriptSource(new File(filePath).getParent()));
//
//                codeUnit = PascalCompiler.loadPascal(new File(filePath).getName(),
//                        new FileReader(filePath), searchPath, new ProgramHandler(filePath));
//                if (codeUnit != null) {
//                    if (((PascalProgramDeclaration) codeUnit).main == null) {
//                        showErrorDialog(new MainProgramNotFoundException());
//                        return false;
//                    }
//                }
//            }
            buildSuggestData();
        } catch (Exception e) {
            showErrorDialog(e);
            return false;
        }
        Toast.makeText(this, R.string.compile_ok, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void buildSuggestData() {

    }

    private void showErrorDialog(Exception e) {
//        this.mDialog = DialogManager.Companion.createErrorDialog(this, e);
//        this.mDialog.show();
//        DLog.e(e);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getPreferences().isShowListSymbol()) {
            mKeyList.setListener(this);
            mContainerSymbol.setVisibility(View.VISIBLE);
        } else {
            mContainerSymbol.setVisibility(View.GONE);
        }
    }


    @Override
    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, @NonNull String s) {
        if (s.equals(getString(R.string.key_show_suggest_popup))
                || s.equals(getString(R.string.key_show_line_number))
                || s.equals(getString(R.string.key_pref_word_wrap))) {
            EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
            if (editorFragment != null) {
                editorFragment.refreshCodeEditor();
            }
        } else if (s.equals(getString(R.string.key_show_symbol))) {
            mContainerSymbol.setVisibility(getPreferences().isShowListSymbol()
                    ? View.VISIBLE : View.GONE);
        } else if (s.equals(getString(R.string.key_show_suggest_popup))) {
            EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
            if (editorFragment != null) {
                EditorView editor = editorFragment.getEditor();
                editor.setSuggestData(new ArrayList<InfoItem>());
            }
        }
        //toggle ime/no suggest mode
        else if (s.equalsIgnoreCase(getString(R.string.key_ime_keyboard))) {
            EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
            if (editorFragment != null) {
                EditorView editor = editorFragment.getEditor();
                editorFragment.refreshCodeEditor();
            }
        } else {
            super.onSharedPreferenceChanged(sharedPreferences, s);
        }
    }

    @Override
    public void onFileClick(File file) {
        //save current file
        addNewPageEditor(file, SELECT);
        //close drawer
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onFileLongClick(File file) {
        showFileInfo(file);
    }


    /**
     * show dialog with file info
     * filePath, path, size, extension ...
     *
     * @param file - file to show info
     */
    private void showFileInfo(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(file.getName());
        builder.setView(R.layout.dialog_view_file);
        AlertDialog dialog = builder.create();
        dialog.show();
        TextView txtInfo = (TextView) dialog.findViewById(R.id.txt_info);
        txtInfo.setText(file.getPath());
        EditorView editorView = (EditorView) dialog.findViewById(R.id.editor_view);
        editorView.setTextHighlighted(mFileManager.fileToString(file));
    }

    /**
     * show dialog create new source file
     */
    @Override
    public void createNewSourceFile(View view) {
        DialogCreateNewFile dialogCreateNewFile = DialogCreateNewFile.Companion.getInstance();
        dialogCreateNewFile.show(getSupportFragmentManager(), DialogCreateNewFile.Companion.getTAG());
        dialogCreateNewFile.setListener(new DialogCreateNewFile.OnCreateNewFileListener() {
            @Override
            public void onFileCreated(@NonNull File file) {
                saveFile();
                //add to view
                addNewPageEditor(file, SELECT);
                mDrawerLayout.closeDrawers();
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    public void goToLine() {
        final AppCompatEditText edittext = new AppCompatEditText(this);
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittext.setMaxEms(5);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.goto_line)
                .setView(edittext)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String line = edittext.getText().toString();
                        if (!line.isEmpty()) {
                            EditorFragment editorFragment
                                    = pagerAdapter.getCurrentFragment();
                            if (editorFragment != null) {
                                editorFragment.goToLine(Integer.parseInt(line));
                            }
                        }
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    @Override
    public void formatCode() {
        EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.formatCode();
        }
    }


    @Override
    public void reportBug() {
        DialogManager.Companion.createDialogReportBug(this, getCode());
    }

    @Override
    public void openTool() {
        mDrawerLayout.openDrawer(GravityCompat.END);
    }

    @Override
    public void undo() {
        EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.undo();
        }
    }

    @Override
    public void redo() {
        EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.redo();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTION_FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    // Get the path
                    String path;
                    try {
                        path = mFileManager.getPath(this, uri);
                        mFileManager.setWorkingFilePath(path);
                        addNewPageEditor(new File(path), SELECT);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case ACTION_PICK_MEDIA_URL:
                if (resultCode == RESULT_OK) {
                    String path = data.getData().toString();
                    EditorFragment currentFragment = pagerAdapter.getCurrentFragment();
                    if (currentFragment != null && path != null) {
                        currentFragment.insert(path);
                    }
                }
                break;
            case ACTION_CREATE_SHORTCUT:
                data.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                getApplicationContext().sendBroadcast(data);
                break;
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        closeKeyBoard();
    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void paste() {
        EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.paste();
        }
    }

    @Override
    public void copyAll() {
        EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.copyAll();
        }
    }

    @Override
    public void selectThemeFont() {
        startActivity(new Intent(this, ThemeFontActivity.class));
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)
                || mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        /*
          check can undo
         */
        if (getPreferences().getBoolean(getString(R.string.key_back_undo))) {
            undo();
            return;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit)
                .setMessage(R.string.exit_mgs)
                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditorActivity.super.onBackPressed();
                    }
                })
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    public void openDrawer(int gravity) {
        mDrawerLayout.openDrawer(gravity);
    }

    private String getCurrentFilePath() {
        EditorFragment editorFragment = pagerAdapter.getCurrentFragment();
        if (editorFragment != null) {
            return editorFragment.getFilePath();
        }
        return "";
    }

    public void showProgramStructure() {
//        try {
//            String filePath = getCurrentFilePath();
//            PascalProgramDeclaration pascalProgram = PascalCompiler
//                    .loadPascal(filePath, new FileReader(filePath),
//                            new ArrayList<ScriptSource>(), null);
//
//            if (pascalProgram.main == null) {
//                showErrorDialog(new MainProgramNotFoundException());
//            }
//            ExpressionContextMixin program = pascalProgram.getProgram();
//
//            com.duy.frontend.structure.viewholder.StructureItem node = getNode(program, pascalProgram.getProgramName(), StructureType.TYPE_PROGRAM, 0);
//
//            DialogProgramStructure dialog = DialogProgramStructure.newInstance(node);
//            dialog.show(getSupportFragmentManager(), DialogProgramStructure.TAG);
//        } catch (Exception e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
    }


    public void startDebug() {
        if (doCompile()) mCompileManager.debug(getCurrentFilePath());
    }

    public void insertColor() {
        ColorPickerDialogBuilder.with(this).
                setPositiveButton(getString(R.string.select), new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        EditorFragment currentFragment = pagerAdapter.getCurrentFragment();
                        if (currentFragment != null) {
                            currentFragment.insert(String.valueOf(lastSelectedColor));
                            Toast.makeText(EditorActivity.this, getString(R.string.inserted_color) + lastSelectedColor,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).build().show();
    }


}