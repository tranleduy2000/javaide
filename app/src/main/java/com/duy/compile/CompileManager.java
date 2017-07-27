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

package com.duy.compile;

import android.app.Activity;
import android.content.Intent;

import com.duy.compile.external.CommandManager;
import com.duy.ide.autocomplete.util.JavaUtil;
import com.duy.ide.debug.activities.DebugActivity;
import com.duy.ide.editor.MainActivity;
import com.duy.project.ClassFile;
import com.duy.project.ProjectFile;
import com.duy.project.utils.ClassUtil;
import com.duy.run.activities.TerminalActivity;

import java.io.File;

/**
 * Created by Duy on 11-Feb-17.
 */

public class CompileManager {

    public static final String FILE_PATH = "file_name";     // extras indicators
    public static final String IS_NEW = "is_new";
    public static final String INITIAL_POS = "initial_pos";
    public static final int ACTIVITY_EDITOR = 1001;
    public static final String MODE = "run_mode";

    public static final String PROJECT_FILE = "project_file";
    public static final String ACTION = "action";
    public static final String ARGS = "program_args";
    public static final String DEX_FILE = "dex_path";

    private final Activity mActivity;

    public CompileManager(Activity activity) {
        this.mActivity = activity;
    }


    public static void debug(Activity mActivity, String name) {
        Intent intent = new Intent(mActivity, DebugActivity.class);
        intent.putExtra(FILE_PATH, name);
        mActivity.startActivity(intent);
    }

    public static void execute(Activity activity, String name) {
        Intent intent = new Intent(activity, TerminalActivity.class);
        intent.putExtra(FILE_PATH, name);
        activity.finish();
        activity.startActivity(intent);
    }


    public void debug(String name) {
        Intent intent = new Intent(mActivity, DebugActivity.class);
        intent.putExtra(FILE_PATH, name);
        mActivity.startActivity(intent);
    }

    public void edit(String fileName, Boolean isNew) {
        Intent intent = new Intent(mActivity, MainActivity.class);
        intent.putExtra(FILE_PATH, fileName);
        intent.putExtra(IS_NEW, isNew);
        intent.putExtra(INITIAL_POS, 0);
        mActivity.startActivityForResult(intent, ACTIVITY_EDITOR);
    }

    // Execute compiled file
    public void execute(ProjectFile projectFile) {
        Intent intent = new Intent(mActivity, TerminalActivity.class);
        intent.putExtra(ACTION, CommandManager.Action.RUN);
        intent.putExtra(PROJECT_FILE, projectFile);
        mActivity.startActivity(intent);
    }

    public void buildJar(ProjectFile projectFile) {
        Intent intent = new Intent(mActivity, TerminalActivity.class);
        intent.putExtra(ACTION, CommandManager.Action.BUILD_JAR);
        intent.putExtra(PROJECT_FILE, projectFile);
        mActivity.startActivity(intent);
    }

    public void executeDex(ProjectFile projectFile, File dex) {
        Intent intent = new Intent(mActivity, TerminalActivity.class);
        intent.putExtra(ACTION, CommandManager.Action.RUN_DEX);
        intent.putExtra(PROJECT_FILE, projectFile);
        intent.putExtra(DEX_FILE, dex);
        mActivity.startActivity(intent);
    }

    public void runFile(ProjectFile projectFile, String filePath, ProcessCallback callback) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                callback.onFailed("File not found");
                return;
            }
            if (file.isDirectory()) {
                callback.onFailed("Not a file");
                return;

            }
            boolean canRun = ClassUtil.hasMainFunction(file);
            if (!canRun) {
                callback.onFailed("Can not find main function");
                return;
            }
            ProjectFile newProject = projectFile.clone();
            String className = JavaUtil.getClassName(projectFile.getRootDir(), filePath);
            if (className == null) {
                callback.onFailed("Class \"" + filePath + "\"" + "invalid");
                return;
            }
            newProject.setMainClass(new ClassFile(className));
            execute(newProject);
        } catch (Exception e) {
            //file exception
        }
    }

    public interface ProcessCallback {
        void onFailed(String s);
    }
}
