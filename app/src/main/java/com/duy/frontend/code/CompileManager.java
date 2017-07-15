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

package com.duy.frontend.code;

import android.app.Activity;
import android.content.Intent;

import com.duy.frontend.debug.activities.DebugActivity;
import com.duy.frontend.editor.EditorActivity;
import com.duy.frontend.runnable.ExecuteActivity;

/**
 * Created by Duy on 11-Feb-17.
 */

public class CompileManager {

    public static final String FILE_PATH = "file_name";     // extras indicators
    public static final String IS_NEW = "is_new";
    public static final String INITIAL_POS = "initial_pos";
    public static final int ACTIVITY_EDITOR = 1001;
    public static final String MODE = "run_mode";
    private final Activity mActivity;

    public CompileManager(Activity activity) {
        this.mActivity = activity;
    }

    public static void execute(Activity activity, String name) {
        Intent intent = new Intent(activity, ExecuteActivity.class);
        intent.putExtra(FILE_PATH, name);
        activity.finish();
        activity.startActivity(intent);
    }

    public static void debug(Activity mActivity, String name) {
        Intent intent = new Intent(mActivity, DebugActivity.class);
        intent.putExtra(FILE_PATH, name);
        mActivity.startActivity(intent);
    }

    // Execute compiled file
    public void execute(String name) {
        Intent intent = new Intent(mActivity, ExecuteActivity.class);
        intent.putExtra(FILE_PATH, name);
        mActivity.startActivity(intent);
    }

    public void debug(String name) {
        Intent intent = new Intent(mActivity, DebugActivity.class);
        intent.putExtra(FILE_PATH, name);
        mActivity.startActivity(intent);
    }

    public void edit(String fileName, Boolean isNew) {
        Intent intent = new Intent(mActivity, EditorActivity.class);
        intent.putExtra(FILE_PATH, fileName);
        intent.putExtra(IS_NEW, isNew);
        intent.putExtra(INITIAL_POS, 0);
        mActivity.startActivityForResult(intent, ACTIVITY_EDITOR);
    }

}
