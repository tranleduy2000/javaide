package com.nolanlawson.logcat.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nolanlawson.logcat.util.UtilLogger;

public class CatlogDBHelper extends SQLiteOpenHelper {

	private static UtilLogger log = new UtilLogger(CatlogDBHelper.class);
	
	private static final String DB_NAME = "catlog.db";
	private static final int DB_VERSION = 1;
	
	private static final String TABLE_NAME = "Filters";
	
	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_TEXT = "filterText";
	
	private SQLiteDatabase db;

	public CatlogDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		db = getWritableDatabase();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createSql = "create table if not exists " + TABLE_NAME + " ("
				+ COLUMN_ID + " integer not null primary key autoincrement, "
				+ COLUMN_TEXT + " text);";
		
		String indexSql = "create unique index if not exists index_game_id on " + TABLE_NAME
				+ " (" + COLUMN_TEXT + ");";
		
		db.execSQL(createSql);
		db.execSQL(indexSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// do nothing
	}
	
	public List<FilterItem> findFilterItems() {
		
		synchronized (CatlogDBHelper.class) {
			
			List<FilterItem> filters = new ArrayList<FilterItem>();
			
			Cursor cursor = null;
			try {
				cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TEXT}, null, null, null, null, null);
				
				while (cursor.moveToNext()) {
					FilterItem filterItem = FilterItem.create(cursor.getInt(0), cursor.getString(1));
					filters.add(filterItem);
				}
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			
			log.d("fetched %d filters", filters.size());
			
			return filters;
		}
	}
	
	public void deleteFilter(int id) {
		synchronized (CatlogDBHelper.class) {
			int rows = db.delete(TABLE_NAME, COLUMN_ID + "=" + id, null);
			log.d("deleted %d filters with id %d", rows, id);
		}
	}
	
	public FilterItem addFilter(String text) {
		synchronized (CatlogDBHelper.class) {
			
			ContentValues contentValues = new ContentValues();
			contentValues.put(COLUMN_TEXT, text);
			
			long result = db.insert(TABLE_NAME, null, contentValues);
			
			log.d("inserted filter with text %s: %g", text, result);
			
			if (result == -1) {
				log.d("attempted to insert duplicate filter");
				return null;
			}
			
			Cursor cursor = null;
			try {
				String selection = COLUMN_TEXT +"=?";
				String[] selectionArgs = {text}; 
				cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TEXT}, selection, selectionArgs, null, null, null);
				cursor.moveToNext();
				return FilterItem.create(cursor.getInt(0), cursor.getString(1));
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
	}
}
