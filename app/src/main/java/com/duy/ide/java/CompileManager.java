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

import android.app.Activity;

import com.duy.android.compiler.project.JavaProject;

/**
 * Created by Duy on 11-Feb-17.
 */

public class CompileManager {
    public static final String FILE_PATH = "file_name";     // extras indicators

    public static final String ACTION = "action";
    public static final String ARGS = "program_args";
    public static final String DEX_FILE = "dex_path";

    private final Activity mActivity;

    public CompileManager(Activity activity) {
        this.mActivity = activity;
    }


    public void executeDex(JavaProject projectFile) {

    }

}
