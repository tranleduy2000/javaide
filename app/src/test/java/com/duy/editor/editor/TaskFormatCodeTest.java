package com.duy.editor.editor;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

import junit.framework.TestCase;

import net.barenca.jastyle.ASFormatter;
import net.barenca.jastyle.FormatterHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by Duy on 16-Jul-17.
 */
public class TaskFormatCodeTest extends TestCase {

    public void test1() throws FormatterException {
        String sourceString = "/*\n" +
                " *  Copyright (c) 2017 Tran Le Duy\n" +
                " *\n" +
                " * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                " * you may not use this file except in compliance with the License.\n" +
                " * You may obtain a copy of the License at\n" +
                " *\n" +
                " *     http://www.apache.org/licenses/LICENSE-2.0\n" +
                " *\n" +
                " * Unless required by applicable law or agreed to in writing, software\n" +
                " * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                " * See the License for the specific language governing permissions and\n" +
                " * limitations under the License.\n" +
                " */\n" +
                "\n" +
                "package com.duy.editor.editor;\n" +
                "\n" +
                "import android.app.Dialog;\n" +
                "import android.content.DialogInterface;\n" +
                "import android.content.Intent;\n" +
                "import android.content.SharedPreferences;\n" +
                "import android.net.Uri;\n" +
                "import android.os.Bundle;\n" +
                "import android.support.annotation.NonNull;\n" +
                "import android.support.design.widget.NavigationView;\n" +
                "import android.support.v4.view.GravityCompat;\n" +
                "import android.support.v4.widget.DrawerLayout;\n" +
                "import android.support.v7.app.AlertDialog;\n" +
                "import android.support.v7.widget.AppCompatEditText;\n" +
                "import android.text.InputType;\n" +
                "import android.view.Menu;\n" +
                "import android.view.MenuItem;\n" +
                "import android.view.View;\n" +
                "import android.widget.CheckBox;\n" +
                "import android.widget.EditText;\n" +
                "import android.widget.TextView;\n" +
                "import android.widget.Toast;\n" +
                "\n" +
                "import com.duy.editor.MenuEditor;\n" +
                "import com.duy.editor.R;\n" +
                "import com.duy.editor.code.CompileManager;\n" +
                "import com.duy.editor.code_sample.activities.DocumentActivity;\n" +
                "import com.duy.editor.dialog.DialogCreateNewFile;\n" +
                "import com.duy.editor.dialog.DialogManager;\n" +
                "import com.duy.editor.editor.view.AutoIndentEditText;\n" +
                "import com.duy.editor.editor.view.EditorView;\n" +
                "import com.duy.editor.editor.view.adapters.InfoItem;\n" +
                "import com.duy.editor.setting.JavaPreferences;\n" +
                "import com.duy.editor.themefont.activities.ThemeFontActivity;\n" +
                "import com.duy.editor.utils.DonateUtils;\n" +
                "import com.flask.colorpicker.builder.ColorPickerClickListener;\n" +
                "import com.flask.colorpicker.builder.ColorPickerDialogBuilder;\n" +
                "\n" +
                "import java.io.File;\n" +
                "import java.util.ArrayList;\n" +
                "\n" +
                "public class EditorActivity extends BaseEditorActivity implements\n" +
                "        DrawerLayout.DrawerListener {\n" +
                "\n" +
                "    public static final int ACTION_FILE_SELECT_CODE = 1012;\n" +
                "    public static final int ACTION_PICK_MEDIA_URL = 1013;\n" +
                "    public static final int ACTION_CREATE_SHORTCUT = 1014;\n" +
                "\n" +
                "    private CompileManager mCompileManager;\n" +
                "    private MenuEditor menuEditor;\n" +
                "    private Dialog mDialog;\n" +
                "\n" +
                "    @Override\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        mCompileManager = new CompileManager(this);\n" +
                "        mDrawerLayout.addDrawerListener(this);\n" +
                "\n" +
                "        menuEditor = new MenuEditor(this, this);\n" +
                "        if (DonateUtils.DONATED) {\n" +
                "            Menu menu = navigationView.getMenu();\n" +
                "            menu.findItem(R.id.action_donate).setVisible(false);\n" +
                "        }\n" +
                "        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {\n" +
                "            @Override\n" +
                "            public boolean onNavigationItemSelected(@NonNull MenuItem item) {\n" +
                "                mDrawerLayout.closeDrawers();\n" +
                "                return menuEditor.onOptionsItemSelected(item);\n" +
                "            }\n" +
                "        });\n" +
                "        findViewById(R.id.img_tab).setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                insertTab(v);\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public boolean onOptionsItemSelected(MenuItem item) {\n" +
                "        return menuEditor.onOptionsItemSelected(item);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    @Override\n" +
                "    public void invalidateOptionsMenu() {\n" +
                "        super.invalidateOptionsMenu();\n" +
                "    }\n" +
                "\n" +
                "    void insertTab(View v) {\n" +
                "        onKeyClick(v, AutoIndentEditText.TAB_CHARACTER);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onKeyClick(View view, String text) {\n" +
                "        EditorFragment currentFragment = mPageAdapter.getCurrentFragment();\n" +
                "        if (currentFragment != null) {\n" +
                "            currentFragment.insert(text);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onKeyLongClick(String text) {\n" +
                "        EditorFragment currentFragment = mPageAdapter.getCurrentFragment();\n" +
                "        if (currentFragment != null) {\n" +
                "            currentFragment.insert(text);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public boolean onCreateOptionsMenu(Menu menu) {\n" +
                "        return menuEditor.onCreateOptionsMenu(menu);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * create dialog find and replace\n" +
                "     */\n" +
                "    @Override\n" +
                "    public void findAndReplace() {\n" +
                "        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);\n" +
                "        builder.setView(R.layout.dialog_find_and_replace);\n" +
                "        final AlertDialog alertDialog = builder.create();\n" +
                "        alertDialog.show();\n" +
                "\n" +
                "        final CheckBox ckbRegex = (CheckBox) alertDialog.findViewById(R.id.ckb_regex);\n" +
                "        final CheckBox ckbMatch = (CheckBox) alertDialog.findViewById(R.id.ckb_match_key);\n" +
                "        final EditText editFind = (EditText) alertDialog.findViewById(R.id.txt_find);\n" +
                "        final EditText editReplace = (EditText) alertDialog.findViewById(R.id.edit_replace);\n" +
                "        editFind.setText(getPreferences().getString(JavaPreferences.LAST_FIND));\n" +
                "        alertDialog.findViewById(R.id.btn_replace).setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "                if (editorFragment != null) {\n" +
                "                    editorFragment.doFindAndReplace(\n" +
                "                            editFind.getText().toString(),\n" +
                "                            editReplace.getText().toString(),\n" +
                "                            ckbRegex.isChecked(),\n" +
                "                            ckbMatch.isChecked());\n" +
                "                }\n" +
                "                getPreferences().put(JavaPreferences.LAST_FIND, editFind.getText().toString());\n" +
                "                alertDialog.dismiss();\n" +
                "            }\n" +
                "        });\n" +
                "        alertDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                alertDialog.dismiss();\n" +
                "            }\n" +
                "        });\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void runProgram() {\n" +
                "        mCompileManager.execute(projectFile);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    @Override\n" +
                "    public boolean isAutoSave() {\n" +
                "        return menuEditor.getChecked(R.id.action_auto_save);\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * replace dialog find\n" +
                "     */\n" +
                "    public void showDialogFind() {\n" +
                "        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);\n" +
                "        builder.setView(R.layout.dialog_find);\n" +
                "        final AlertDialog alertDialog = builder.create();\n" +
                "        alertDialog.show();\n" +
                "        final CheckBox ckbRegex = (CheckBox) alertDialog.findViewById(R.id.ckb_regex);\n" +
                "        final CheckBox ckbMatch = (CheckBox) alertDialog.findViewById(R.id.ckb_match_key);\n" +
                "        final CheckBox ckbWordOnly = (CheckBox) alertDialog.findViewById(R.id.ckb_word_only);\n" +
                "        final EditText editFind = (EditText) alertDialog.findViewById(R.id.txt_find);\n" +
                "        editFind.setText(getPreferences().getString(JavaPreferences.LAST_FIND));\n" +
                "        alertDialog.findViewById(R.id.btn_replace).setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "                if (editorFragment != null) {\n" +
                "                    editorFragment.doFind(editFind.getText().toString(),\n" +
                "                            ckbRegex.isChecked(),\n" +
                "                            ckbWordOnly.isChecked(),\n" +
                "                            ckbMatch.isChecked());\n" +
                "                }\n" +
                "                getPreferences().put(JavaPreferences.LAST_FIND, editFind.getText().toString());\n" +
                "                alertDialog.dismiss();\n" +
                "            }\n" +
                "        });\n" +
                "        alertDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                alertDialog.dismiss();\n" +
                "            }\n" +
                "        });\n" +
                "\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void saveFile() {\n" +
                "        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "        if (editorFragment != null) {\n" +
                "            editorFragment.saveFile();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void showDocumentActivity() {\n" +
                "        Intent intent = new Intent(this, DocumentActivity.class);\n" +
                "        startActivity(intent);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    public String getCode() {\n" +
                "        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "        if (editorFragment != null) {\n" +
                "            return editorFragment.getCode();\n" +
                "        }\n" +
                "        return \"\";\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * compile code, if is error, show dialog error\n" +
                "     * invalidate keyword\n" +
                "     */\n" +
                "    @Override\n" +
                "    public boolean doCompile() {\n" +
                "        saveFile();\n" +
                "        String filePath = getCurrentFilePath();\n" +
                "        if (filePath.isEmpty()) return false;\n" +
                "        try {\n" +
                "//            CodeUnit codeUnit;\n" +
                "//            if (getCode().trim().toLowerCase().startsWith(\"unit \")) {\n" +
                "//\n" +
                "//                ArrayList<ScriptSource> searchPath = new ArrayList<>();\n" +
                "//                searchPath.add(new FileScriptSource(new File(filePath).getParent()));\n" +
                "//                codeUnit = PascalCompiler.loadLibrary(new File(filePath).getName(),\n" +
                "//                        new FileReader(filePath),\n" +
                "//                        searchPath,\n" +
                "//                        new ProgramHandler(filePath));\n" +
                "//            } else {\n" +
                "//\n" +
                "//                ArrayList<ScriptSource> searchPath = new ArrayList<>();\n" +
                "//                searchPath.add(new FileScriptSource(new File(filePath).getParent()));\n" +
                "//\n" +
                "//                codeUnit = PascalCompiler.loadPascal(new File(filePath).getName(),\n" +
                "//                        new FileReader(filePath), searchPath, new ProgramHandler(filePath));\n" +
                "//                if (codeUnit != null) {\n" +
                "//                    if (((PascalProgramDeclaration) codeUnit).main == null) {\n" +
                "//                        showErrorDialog(new MainProgramNotFoundException());\n" +
                "//                        return false;\n" +
                "//                    }\n" +
                "//                }\n" +
                "//            }\n" +
                "            buildSuggestData();\n" +
                "        } catch (Exception e) {\n" +
                "            showErrorDialog(e);\n" +
                "            return false;\n" +
                "        }\n" +
                "        Toast.makeText(this, R.string.compile_ok, Toast.LENGTH_SHORT).show();\n" +
                "        return true;\n" +
                "    }\n" +
                "\n" +
                "    private void buildSuggestData() {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    private void showErrorDialog(Exception e) {\n" +
                "//        this.mDialog = DialogManager.Companion.createErrorDialog(this, e);\n" +
                "//        this.mDialog.show();\n" +
                "//        DLog.e(e);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    protected void onDestroy() {\n" +
                "        super.onDestroy();\n" +
                "        if (mDialog != null && mDialog.isShowing()) {\n" +
                "            mDialog.dismiss();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    protected void onResume() {\n" +
                "        super.onResume();\n" +
                "        if (getPreferences().isShowListSymbol()) {\n" +
                "            mKeyList.setListener(this);\n" +
                "            mContainerSymbol.setVisibility(View.VISIBLE);\n" +
                "        } else {\n" +
                "            mContainerSymbol.setVisibility(View.GONE);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    @Override\n" +
                "    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, @NonNull String s) {\n" +
                "        if (s.equals(getString(R.string.key_show_suggest_popup))\n" +
                "                || s.equals(getString(R.string.key_show_line_number))\n" +
                "                || s.equals(getString(R.string.key_pref_word_wrap))) {\n" +
                "            EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "            if (editorFragment != null) {\n" +
                "                editorFragment.refreshCodeEditor();\n" +
                "            }\n" +
                "        } else if (s.equals(getString(R.string.key_show_symbol))) {\n" +
                "            mContainerSymbol.setVisibility(getPreferences().isShowListSymbol()\n" +
                "                    ? View.VISIBLE : View.GONE);\n" +
                "        } else if (s.equals(getString(R.string.key_show_suggest_popup))) {\n" +
                "            EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "            if (editorFragment != null) {\n" +
                "                EditorView editor = editorFragment.getEditor();\n" +
                "                editor.setSuggestData(new ArrayList<InfoItem>());\n" +
                "            }\n" +
                "        }\n" +
                "        //toggle ime/no suggest mode\n" +
                "        else if (s.equalsIgnoreCase(getString(R.string.key_ime_keyboard))) {\n" +
                "            EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "            if (editorFragment != null) {\n" +
                "                EditorView editor = editorFragment.getEditor();\n" +
                "                editorFragment.refreshCodeEditor();\n" +
                "            }\n" +
                "        } else {\n" +
                "            super.onSharedPreferenceChanged(sharedPreferences, s);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onFileClick(File file) {\n" +
                "        //save current file\n" +
                "        addNewPageEditor(file, SELECT);\n" +
                "        //close drawer\n" +
                "        mDrawerLayout.closeDrawers();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onFileLongClick(File file) {\n" +
                "        showFileInfo(file);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    /**\n" +
                "     * show dialog with file info\n" +
                "     * filePath, path, size, extension ...\n" +
                "     *\n" +
                "     * @param file - file to show info\n" +
                "     */\n" +
                "    private void showFileInfo(File file) {\n" +
                "        AlertDialog.Builder builder = new AlertDialog.Builder(this);\n" +
                "        builder.setTitle(file.getName());\n" +
                "        builder.setView(R.layout.dialog_view_file);\n" +
                "        AlertDialog dialog = builder.create();\n" +
                "        dialog.show();\n" +
                "        TextView txtInfo = (TextView) dialog.findViewById(R.id.txt_info);\n" +
                "        txtInfo.setText(file.getPath());\n" +
                "        EditorView editorView = (EditorView) dialog.findViewById(R.id.editor_view);\n" +
                "        editorView.setTextHighlighted(mFileManager.fileToString(file));\n" +
                "    }\n" +
                "\n" +
                "    /**\n" +
                "     * show dialog create new source file\n" +
                "     */\n" +
                "    @Override\n" +
                "    public void createNewFile(View view) {\n" +
                "        DialogCreateNewFile dialogCreateNewFile = DialogCreateNewFile.Companion.getInstance();\n" +
                "        dialogCreateNewFile.show(getSupportFragmentManager(), DialogCreateNewFile.Companion.getTAG());\n" +
                "        dialogCreateNewFile.setListener(new DialogCreateNewFile.OnCreateNewFileListener() {\n" +
                "            @Override\n" +
                "            public void onFileCreated(@NonNull File file) {\n" +
                "                saveFile();\n" +
                "                //add to view\n" +
                "                addNewPageEditor(file, SELECT);\n" +
                "                mDrawerLayout.closeDrawers();\n" +
                "            }\n" +
                "\n" +
                "            @Override\n" +
                "            public void onCancel() {\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void goToLine() {\n" +
                "        final AppCompatEditText edittext = new AppCompatEditText(this);\n" +
                "        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);\n" +
                "        edittext.setMaxEms(5);\n" +
                "        AlertDialog.Builder builder = new AlertDialog.Builder(this);\n" +
                "        builder.setTitle(R.string.goto_line)\n" +
                "                .setView(edittext)\n" +
                "                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {\n" +
                "                    public void onClick(DialogInterface dialog, int id) {\n" +
                "                        String line = edittext.getText().toString();\n" +
                "                        if (!line.isEmpty()) {\n" +
                "                            EditorFragment editorFragment\n" +
                "                                    = mPageAdapter.getCurrentFragment();\n" +
                "                            if (editorFragment != null) {\n" +
                "                                editorFragment.goToLine(Integer.parseInt(line));\n" +
                "                            }\n" +
                "                        }\n" +
                "                        dialog.cancel();\n" +
                "                    }\n" +
                "                })\n" +
                "                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {\n" +
                "                    public void onClick(DialogInterface dialog, int id) {\n" +
                "                        dialog.cancel();\n" +
                "                    }\n" +
                "                });\n" +
                "        builder.create().show();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void formatCode() {\n" +
                "        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "        if (editorFragment != null) {\n" +
                "            editorFragment.formatCode();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    @Override\n" +
                "    public void reportBug() {\n" +
                "        DialogManager.Companion.createDialogReportBug(this, getCode());\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void openTool() {\n" +
                "        mDrawerLayout.openDrawer(GravityCompat.END);\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void undo() {\n" +
                "        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "        if (editorFragment != null) {\n" +
                "            editorFragment.undo();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void redo() {\n" +
                "        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "        if (editorFragment != null) {\n" +
                "            editorFragment.redo();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    @Override\n" +
                "    protected void onActivityResult(int requestCode, int resultCode, Intent data) {\n" +
                "        super.onActivityResult(requestCode, resultCode, data);\n" +
                "        switch (requestCode) {\n" +
                "            case ACTION_FILE_SELECT_CODE:\n" +
                "                if (resultCode == RESULT_OK) {\n" +
                "                    // Get the Uri of the selected file\n" +
                "                    Uri uri = data.getData();\n" +
                "                    // Get the path\n" +
                "                    String path;\n" +
                "                    try {\n" +
                "                        path = mFileManager.getPath(this, uri);\n" +
                "                        mFileManager.setWorkingFilePath(path);\n" +
                "                        addNewPageEditor(new File(path), SELECT);\n" +
                "                    } catch (Exception e) {\n" +
                "                        e.printStackTrace();\n" +
                "                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();\n" +
                "                    }\n" +
                "                }\n" +
                "                break;\n" +
                "            case ACTION_PICK_MEDIA_URL:\n" +
                "                if (resultCode == RESULT_OK) {\n" +
                "                    String path = data.getData().toString();\n" +
                "                    EditorFragment currentFragment = mPageAdapter.getCurrentFragment();\n" +
                "                    if (currentFragment != null && path != null) {\n" +
                "                        currentFragment.insert(path);\n" +
                "                    }\n" +
                "                }\n" +
                "                break;\n" +
                "            case ACTION_CREATE_SHORTCUT:\n" +
                "                data.setAction(\"com.android.launcher.action.INSTALL_SHORTCUT\");\n" +
                "                getApplicationContext().sendBroadcast(data);\n" +
                "                break;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onDrawerSlide(View drawerView, float slideOffset) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onDrawerOpened(View drawerView) {\n" +
                "        closeKeyBoard();\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onDrawerClosed(View drawerView) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void paste() {\n" +
                "        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "        if (editorFragment != null) {\n" +
                "            editorFragment.paste();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void copyAll() {\n" +
                "        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "        if (editorFragment != null) {\n" +
                "            editorFragment.copyAll();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void selectThemeFont() {\n" +
                "        startActivity(new Intent(this, ThemeFontActivity.class));\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onDrawerStateChanged(int newState) {\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public void onBackPressed() {\n" +
                "        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)\n" +
                "                || mDrawerLayout.isDrawerOpen(GravityCompat.END)) {\n" +
                "            mDrawerLayout.closeDrawers();\n" +
                "            return;\n" +
                "        }\n" +
                "\n" +
                "        /*\n" +
                "          check can undo\n" +
                "         */\n" +
                "        if (getPreferences().getBoolean(getString(R.string.key_back_undo))) {\n" +
                "            undo();\n" +
                "            return;\n" +
                "        }\n" +
                "\n" +
                "\n" +
                "        AlertDialog.Builder builder = new AlertDialog.Builder(this);\n" +
                "        builder.setTitle(R.string.exit)\n" +
                "                .setMessage(R.string.exit_mgs)\n" +
                "                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {\n" +
                "                    @Override\n" +
                "                    public void onClick(DialogInterface dialog, int which) {\n" +
                "                        EditorActivity.super.onBackPressed();\n" +
                "                    }\n" +
                "                })\n" +
                "                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {\n" +
                "                    @Override\n" +
                "                    public void onClick(DialogInterface dialog, int which) {\n" +
                "                        dialog.cancel();\n" +
                "                    }\n" +
                "                }).create().show();\n" +
                "    }\n" +
                "\n" +
                "    public void openDrawer(int gravity) {\n" +
                "        mDrawerLayout.openDrawer(gravity);\n" +
                "    }\n" +
                "\n" +
                "    private String getCurrentFilePath() {\n" +
                "        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();\n" +
                "        if (editorFragment != null) {\n" +
                "            return editorFragment.getFilePath();\n" +
                "        }\n" +
                "        return \"\";\n" +
                "    }\n" +
                "\n" +
                "    public void showProgramStructure() {\n" +
                "//        try {\n" +
                "//            String filePath = getCurrentFilePath();\n" +
                "//            PascalProgramDeclaration pascalProgram = PascalCompiler\n" +
                "//                    .loadPascal(filePath, new FileReader(filePath),\n" +
                "//                            new ArrayList<ScriptSource>(), null);\n" +
                "//\n" +
                "//            if (pascalProgram.main == null) {\n" +
                "//                showErrorDialog(new MainProgramNotFoundException());\n" +
                "//            }\n" +
                "//            ExpressionContextMixin program = pascalProgram.getProgram();\n" +
                "//\n" +
                "//            com.duy.frontend.structure.viewholder.StructureItem node = getNode(program, pascalProgram.getProgramName(), StructureType.TYPE_PROGRAM, 0);\n" +
                "//\n" +
                "//            DialogProgramStructure dialog = DialogProgramStructure.newInstance(node);\n" +
                "//            dialog.show(getSupportFragmentManager(), DialogProgramStructure.TAG);\n" +
                "//        } catch (Exception e) {\n" +
                "//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();\n" +
                "//        }\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    public void startDebug() {\n" +
                "        if (doCompile()) mCompileManager.debug(getCurrentFilePath());\n" +
                "    }\n" +
                "\n" +
                "    public void insertColor() {\n" +
                "        ColorPickerDialogBuilder.with(this).\n" +
                "                setPositiveButton(getString(R.string.select), new ColorPickerClickListener() {\n" +
                "                    @Override\n" +
                "                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {\n" +
                "                        EditorFragment currentFragment = mPageAdapter.getCurrentFragment();\n" +
                "                        if (currentFragment != null) {\n" +
                "                            currentFragment.insert(String.valueOf(lastSelectedColor));\n" +
                "                            Toast.makeText(EditorActivity.this, getString(R.string.inserted_color) + lastSelectedColor,\n" +
                "                                    Toast.LENGTH_SHORT).show();\n" +
                "                        }\n" +
                "                    }\n" +
                "                })\n" +
                "                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {\n" +
                "                    @Override\n" +
                "                    public void onClick(DialogInterface dialog, int which) {\n" +
                "                        dialog.cancel();\n" +
                "                    }\n" +
                "                }).build().show();\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "}";
        String formattedSource = new Formatter().formatSource(sourceString);
        System.out.println(formattedSource);
    }

    public void test2() throws FormatterException {
        String source = "package com.example;\n" +
                "\n" +
                "import javList;\n" +
                "\n" +
                "public class Main {public static void main(String[] args) {NumberOne numberOne = new NumberOne();numberOne.print();NumberTwo numberTwo = new NumberTwo();ArrayList<String> arrayList = new ArrayList<String>();arrayList.add(numberOne.toString());arrayList.add(numberTwo.toString());System.out.println(arrayList);}\n" +
                "}\n";
        System.out.println(new Formatter().formatSource(source));
    }

    public void test3() throws IOException {
        String source = "package com.example;\n" +
                "\n" +
                "import javList;\n" +
                "\n" +
                "public class Main {public static void main(String[] args) {NumberOne numberOne = new NumberOne();numberOne.print();NumberTwo numberTwo = new NumberTwo();ArrayList<String> arrayList = new ArrayList<String>();arrayList.add(numberOne.toString());arrayList.add(numberTwo.toString());System.out.println(arrayList);}\n" +
                "}\n";
        ASFormatter formatter = new ASFormatter();

        // bug on lib's implementation. reported here: http://barenka.blogspot.com/2009/10/source-code-formatter-library-for-java.html
        source = source.replace("{", "{\n");

        Reader in = new BufferedReader(new StringReader(source));
        formatter.setJavaStyle();
        String format = FormatterHelper.format(in, formatter);
        System.out.println(format);

        in.close();
    }
}