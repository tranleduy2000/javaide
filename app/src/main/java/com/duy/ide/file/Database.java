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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

/**
 * SQ lite database for pascal compiler
 * include history, variable, ...
 * Created by Duy on 3/7/2016
 */
public class Database extends SQLiteOpenHelper implements Serializable {
    private static final String DATABASE_NAME = "db_manager";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_FILE_TAB = "tbl_file_history";
    private static final String KEY_FILE_PATH = "path";


    public static final String CREATE_TABLE_FILE_HISTORY =
            "create table " + TABLE_FILE_TAB +
                    "(" +
                    KEY_FILE_PATH + " TEXT PRIMARY KEY" +
                    ")";

    private String TAG = Database.class.getName();

    public Database(@NonNull Context context,
                    @NonNull String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public Database(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FILE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILE_TAB);
        onCreate(db);
    }

    public Set<File> getListFile() {
        ArraySet<File> files = new android.support.v4.util.ArraySet<>();
        try {
            SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_FILE_TAB;
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    String result = cursor.getString(cursor.getColumnIndex(KEY_FILE_PATH));
                    File file = new File(result);
                    if (file.isFile())
                        files.add(file);
                    else
                        removeFile(result);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception ignored) {

        }
        return files;
    }

    public long addNewFile(File file) {
        try {
            SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_FILE_PATH, file.getPath());
            return sqLiteDatabase.insert(TABLE_FILE_TAB, null, contentValues);
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean removeFile(String path) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.delete(TABLE_FILE_TAB, KEY_FILE_PATH + "=?", new String[]{path}) > 0;
    }

    public long addNewFile(String file) {
        return addNewFile(new File(file));
    }

}
