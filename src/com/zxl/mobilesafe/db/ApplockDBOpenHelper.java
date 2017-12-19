package com.zxl.mobilesafe.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ApplockDBOpenHelper extends SQLiteOpenHelper {

	/**
	 * 数据库创建的构造方法 数据库名称 applock.db
	 * @param context
	 */
	public ApplockDBOpenHelper(Context context) {
		super(context, "applock", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table applock (_id integer primary key autoincrement, packname varchar(20))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
