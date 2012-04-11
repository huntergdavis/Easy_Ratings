package com.hunterdavis.easyratings;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;


public class InventorySQLHelper extends android.database.sqlite.SQLiteOpenHelper {
	private static final String DATABASE_NAME = "easyratings.db";
	private static final int DATABASE_VERSION = 1;

	// Table name
	public static final String TABLE = "ratings";

	// Columns
	public static final String RATING = "rating";
	public static final String CATEGORY = "category";
	public static final String EXTRAINFO = "extrainfo";
	public static final String NAME = "name";
	public static final String IMAGEURI = "imageuri";
	
	
	public static final String CATTABLE = "categories";
	public static final String CATNAME = "name";

	public InventorySQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table " + TABLE + "( " + BaseColumns._ID
				+ " integer primary key autoincrement, " + RATING
				+ " float, " + CATEGORY + " text not null, " + IMAGEURI + " text not null, " + EXTRAINFO
				+ " text, "+ NAME + " text not null);";
		Log.d("Inventory", "onCreate: " + sql);
		db.execSQL(sql);
		
		sql = "create table " + CATTABLE + "( " + BaseColumns._ID
				+ " integer primary key autoincrement, " + CATNAME + " text not null);";
		Log.d("Inventory", "onCreate: " + sql);
		db.execSQL(sql);
		
		
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion)
			return;

		String sql = null;
		if (oldVersion == 1)
			sql = "alter table " + TABLE + " add note text;";
		if (oldVersion == 2)
			sql = "";

		if (sql != null)
			db.execSQL(sql);
	}

}
