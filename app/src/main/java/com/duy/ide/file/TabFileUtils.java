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

package com.duy.ide.file;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 17-Mar-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class TabFileUtils {
    public static ArrayList<File> getTabFiles(Context context) {
        ArrayList<File> files = new ArrayList<>();
        Database database = new Database(context);
        files.addAll(database.getListFile());
        return files;
    }
}
