package com.zxl.mobilesafe.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NumberAddressQueryUtils {

	private static String path = "data/data/com.zxl.mobilesafe/files/address.db";

	public static String queryNumber(String number) {
		String address = number;
		// path ��address.db������ݿ⿽����data/data/��������/files/address.db
		SQLiteDatabase database = SQLiteDatabase.openDatabase(path, null,
				SQLiteDatabase.OPEN_READONLY);

		// �ֻ����� 13 14 15 16 18
		// �ֻ������������ʽ
		if (number.matches("^1[34568]\\d{9}$")) {
			//�ֻ�����
			Cursor cursor = database
					.rawQuery(
							"select location from data2 where id = (select outkey from data1 where id = ?)",
							new String[] { number.substring(0, 7) });
			
			while (cursor.moveToNext()) {
				String location = cursor.getString(0);
				address = location;
			}
			cursor.close();
		} else {
			//�����ĺ���
			switch (number.length()) {
			case 3:
				// 110
				address = "�������";
				break;
			case 4:
				//5554
				address = "ģ����";
				break;
			case 5:
				//10086
				address = "�ͷ��绰";
				break;
			case 7:
				address = "���غ���";
				break;
			case 8:
				address = "���غ���";
				break;
			default:
				// /����;�绰 10
				if (number.length() > 10 && number.startsWith("0")) {
					// 010-59790386
					Cursor cursor = database.rawQuery(
							"select location from data2 where area = ?",
							new String[] { number.substring(1, 3) });

					while (cursor.moveToNext()) {
						String location = cursor.getString(0);
						address = location.substring(0, location.length() - 2);
					}
					cursor.close();

					// 0855-59790386
					cursor = database.rawQuery(
							"select location from data2 where area = ?",
							new String[] { number.substring(1, 4) });
					while (cursor.moveToNext()) {
						String location = cursor.getString(0);
						address = location.substring(0, location.length() - 2);

					}
				}
				break;
			}
		}

		return address;
	}
}
