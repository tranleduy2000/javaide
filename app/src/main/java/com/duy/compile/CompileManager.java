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

import com.duy.ide.debug.activities.DebugActivity;
import com.duy.ide.editor.code.MainActivity;
import com.duy.project.file.java.JavaProject;
import com.duy.run.activities.ExecuteActivity;

import java.io.File;

/**
 * Created by Duy on 11-Feb-17.
 */

public class CompileManager {
    public static final String FILE_PATH = "file_name";     // extras indicators
    public static final String IS_NEW = "is_new";
    public static final String INITIAL_POS = "initial_pos";
    public static final int ACTIVITY_EDITOR = 1001;

    public static final String PROJECT_FILE = "project_file";
    public static final String ACTION = "action";
    public static final String ARGS = "program_args";
    public static final String DEX_FILE = "dex_path";

    private final Activity mActivity;

    public CompileManager(Activity activity) {
        this.mActivity = activity;
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


    public void executeDex(JavaProject projectFile, File dex) {
        Intent intent = new Intent(mActivity, ExecuteActivity.class);
        intent.putExtra(ACTION, ExecuteActivity.RUN_DEX);
        intent.putExtra(PROJECT_FILE, projectFile);
        intent.putExtra(DEX_FILE, dex);
        mActivity.startActivity(intent);
    }

}
