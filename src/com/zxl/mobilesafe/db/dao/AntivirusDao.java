package com.zxl.mobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AntivirusDao {

	public static final String path = "/data/data/com.zxl.mobilesafe/files/antivirus.db";
	
	/**
	 * ��ѯ�Ƿ��ǲ���
	 * @param md5
	 * @return ������������Ϣ�����Ϊnull���ǲ���
	 */
	public static String isVirus(String md5) {
		String result = null;
		SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		Cursor cursor = db.rawQuery("select desc from datable where md5 =?", new String[]{md5});
		if(cursor.moveToNext()) {
			result = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return result;
	}
}
