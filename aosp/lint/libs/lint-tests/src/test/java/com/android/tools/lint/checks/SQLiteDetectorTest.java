/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

public class SQLiteDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new SQLiteDetector();
    }

    public void test() throws Exception {
        assertEquals(""
                + "src/test/pkg/SQLiteTest.java:25: Warning: Using column type STRING; did you mean to use TEXT? (STRING is a numeric type and its value can be adjusted; for example,strings that look like integers can drop leading zeroes. See issue explanation for details.) [SQLiteString]\n"
                + "        db.execSQL(\"CREATE TABLE \" + name + \"(\" + Tables.AppKeys.SCHEMA + \");\"); // ERROR\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/SQLiteTest.java:30: Warning: Using column type STRING; did you mean to use TEXT? (STRING is a numeric type and its value can be adjusted; for example,strings that look like integers can drop leading zeroes. See issue explanation for details.) [SQLiteString]\n"
                + "        db.execSQL(TracksColumns.CREATE_TABLE); // ERROR\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 2 warnings\n",

                lintProject(
                        "src/test/pkg/MyTracksProvider.java.txt=>src/test/pkg/MyTracksProvider.java",
                        "src/test/pkg/SQLiteTest.java.txt=>src/test/pkg/SQLiteTest.java",
                        // stub for type resolution
                        "src/test/pkg/SQLiteDatabase.java.txt=>src/android/database/sqlite/SQLiteDatabase.java"
                ));
    }
}
