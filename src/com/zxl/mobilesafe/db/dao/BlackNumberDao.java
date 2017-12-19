package com.zxl.mobilesafe.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zxl.mobilesafe.db.BlackNumberDBOpenHelper;
import com.zxl.mobilesafe.domain.BlackNumberInfo;

/**
 * 黑名单数据库的增删改查业务类
 * @author ZXL
 *
 */
public class BlackNumberDao {
	private BlackNumberDBOpenHelper helper;
	/**
	 * 构造方法
	 * @param context 上下文
	 */
	public BlackNumberDao(Context context) {
		helper = new BlackNumberDBOpenHelper(context);
	}
	/**
	 * 查询黑名单号码是否存在
	 * @param number
	 * @return
	 */
	public boolean find(String number) {
		boolean result = false;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from blacknumber where number=?", new String[]{number});
		if(cursor.moveToNext()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}
	/**
	 * 查询黑名单号码的拦截模式
	 * @param number
	 * @return 返回号码的拦截模式，不是黑名单号码返回null
	 */
	public String findMode(String number) {
		String result = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select mode from blacknumber where number=?", new String[]{number});
		if(cursor.moveToNext()) {
			result = cursor.getString(0);
		}
		cursor.close();
		db.close();
		return result;
	}
	/**
	 * 查询全部黑名单号码
	 * @return
	 */
	public List<BlackNumberInfo> findAll() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<BlackNumberInfo> result = new ArrayList<BlackNumberInfo>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select number, mode from blacknumber order by _id desc", null);
		while(cursor.moveToNext()) {
			BlackNumberInfo info = new BlackNumberInfo();
			String number = cursor.getString(0);
			String mode = cursor.getString(1);
			info.setMode(mode);
			info.setNumber(number);
			result.add(info);
		}
		cursor.close();
		db.close();
		return result;
	}
	/**
	 * 查询部分的黑名单号码
	 * @param offset从哪个位置开始获取数据
	 * @param maxnumber 一次最多获取多少条记录
	 * @return
	 */
	public List<BlackNumberInfo> findPart(int offset, int maxnumber) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<BlackNumberInfo> result = new ArrayList<BlackNumberInfo>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select number, mode from blacknumber order by _id desc limit ? offset ?",
				new String[]{String.valueOf(maxnumber), String.valueOf(offset)});
		while(cursor.moveToNext()) {
			BlackNumberInfo info = new BlackNumberInfo();
			String number = cursor.getString(0);
			String mode = cursor.getString(1);
			info.setMode(mode);
			info.setNumber(number);
			result.add(info);
		}
		cursor.close();
		db.close();
		return result;
	}
	/**
	 * 添加黑名单号码
	 * @param number  黑名单号码
	 * @param mode  拦截模式  1.电话拦截  2.短信拦截  3.全部拦截
	 */
	public void add(String number, String mode) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("number", number);
		values.put("mode", mode);
		db.insert("blacknumber", null, values);
		db.close();
	}
	/**
	 * 修改黑名单号码的拦截模式
	 * @param number  要修改的黑名单号码
	 * @param newmode  新的拦截模式
	 */
	public void update(String number, String newmode) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("mode", newmode);
		db.update("blacknumber", values, "number=?", new String[]{number});
		db.close();
	}
	/**
	 * 删除黑名单号码
	 * @param number  要删除的黑名单号码
	 */
	public void delete(String number) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete("blacknumber", "number=?", new String[]{number});
		db.close();
	}
}
