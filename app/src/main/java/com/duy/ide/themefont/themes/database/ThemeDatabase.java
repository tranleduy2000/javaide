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

package com.duy.ide.themefont.themes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

import static com.duy.ide.themefont.themes.database.CodeTheme.BACKGROUND;
import static com.duy.ide.themefont.themes.database.CodeTheme.BOOLEAN;
import static com.duy.ide.themefont.themes.database.CodeTheme.COMMENT;
import static com.duy.ide.themefont.themes.database.CodeTheme.ERROR;
import static com.duy.ide.themefont.themes.database.CodeTheme.KEY_WORD;
import static com.duy.ide.themefont.themes.database.CodeTheme.NAME;
import static com.duy.ide.themefont.themes.database.CodeTheme.NORMAL;
import static com.duy.ide.themefont.themes.database.CodeTheme.NUMBER;
import static com.duy.ide.themefont.themes.database.CodeTheme.OPERATOR;
import static com.duy.ide.themefont.themes.database.CodeTheme.STRING;
import static com.duy.ide.themefont.themes.database.CodeTheme.TABLE_NAME;

/**
 * Created by Duy on 12-Jul-17.
 */

public class ThemeDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "themes.db";
    private static final String SQL_CREATE_TABLE = "Create table " + TABLE_NAME + "(" +
            NAME + " TEXT PRIMARY KEY, " +
            BACKGROUND + " INTEGER, " +
            NORMAL + " INTEGER, " +
            KEY_WORD + " INTEGER, " +
            BOOLEAN + " INTEGER, " +
            ERROR + " INTEGER, " +
            NUMBER + " INTEGER, " +
            OPERATOR + " INTEGER, " +
            COMMENT + " INTEGER, " +
            STRING + " INTEGER)";
    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final int DB_VERSION = 1;
    private static final String TAG = "ThemeDatabase";


    public ThemeDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public long insert(CodeTheme themeEntry) {
        Log.d(TAG, "insert() called with: themeEntry = [" + themeEntry + "]");

        ContentValues contentValues = new ContentValues();
        for (Map.Entry<String, Integer> entry : themeEntry.getColors().entrySet()) {
            contentValues.put(entry.getKey(), entry.getValue());
        }
        contentValues.put(NAME, themeEntry.getName());
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_NAME, null, contentValues);
    }

    public boolean hasValue(@NonNull String name) {
        Log.d(TAG, "hasValue() called with: name = [" + name + "]");

        SQLiteDatabase db = getReadableDatabase();
        Cursor query = db.query(TABLE_NAME, new String[]{NAME}, NAME + "=?",
                new String[]{name}, null, null, null);
        boolean r = query.moveToFirst();
        query.close();
        return r;
    }

    public ArrayList<CodeTheme> getAll() {
        Log.d(TAG, "getAll() called");

        String[] projection = {NAME, BACKGROUND, NORMAL, KEY_WORD, BOOLEAN,
                ERROR, NUMBER, OPERATOR, COMMENT, STRING};
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<CodeTheme> codeThemes = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, projection, null, null, null, null, null);
        while (cursor.moveToNext()) {
            CodeTheme codeTheme = new CodeTheme(false);
            codeTheme.setName(cursor.getString(cursor.getColumnIndex(NAME)));
            codeTheme.setBackgroundColor(cursor.getInt(cursor.getColumnIndex(BACKGROUND)));
            codeTheme.setTextColor(cursor.getInt(cursor.getColumnIndex(NORMAL)));
            codeTheme.setKeyWordColor(cursor.getInt(cursor.getColumnIndex(KEY_WORD)));
            codeTheme.setBooleanColor(cursor.getInt(cursor.getColumnIndex(BOOLEAN)));
            codeTheme.setErrorColor(cursor.getInt(cursor.getColumnIndex(ERROR)));
            codeTheme.setNumberColor(cursor.getInt(cursor.getColumnIndex(NUMBER)));
            codeTheme.setOptColor(cursor.getInt(cursor.getColumnIndex(OPERATOR)));
            codeTheme.setCommentColor(cursor.getInt(cursor.getColumnIndex(COMMENT)));
            codeTheme.setStringColor(cursor.getInt(cursor.getColumnIndex(STRING)));
            codeThemes.add(codeTheme);
        }
        cursor.close();
        Log.d(TAG, "getAll() returned: " + codeThemes);
        return codeThemes;
    }

    public int delete(CodeTheme codeTheme) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, NAME + "=?", new String[]{codeTheme.getName()});
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DROP_TABLE);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        onUpgrade(db, oldVersion, newVersion);
    }


}
