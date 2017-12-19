package com.zxl.mobilesafe.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;

/**
 * 短信的工具类
 * 
 * @author ZXL
 * 
 */
public class SmsUtils {

	// 备份短信的回调接口
	public interface BackUpCallBack {
		// 开始备份的时候，设置进度的最大值
		public void beforeBackup(int max);

		// 备份过程中，增加进度
		public void onSmsBackup(int progress);
	}

	/**
	 * 备份用户的短信
	 * 
	 * @param context
	 *            上下文
	 */
	public static void backupSms(Context context, BackUpCallBack callBack)
			throws Exception {
		ContentResolver resolver = context.getContentResolver();
		File file = new File(Environment.getExternalStorageDirectory(),
				"backup.xml");
		FileOutputStream fos = new FileOutputStream(file);
		// 把用户的短信一条一条读出来，按照一定的格式写到文件里
		XmlSerializer serializer = Xml.newSerializer();// 获取xml文件的生成器（序列化器）
		// 初始化生成器
		serializer.setOutput(fos, "utf-8");
		serializer.startDocument("utf-8", true);
		serializer.startTag(null, "smss");
		Uri uri = Uri.parse("content://sms/");
		Cursor cursor = resolver.query(uri, new String[] { "body", "address",
				"type", "date" }, null, null, null);
		// 开始备份的时候，设置进度条的最大值
		int max = cursor.getCount();
		// pd.setMax(max);
		// callBack.beforeBackup(max);
		// serializer.attribute(null, "max", max + "");
		int process = 0;
		while (cursor.moveToNext()) {
			Thread.sleep(500);
			String body = cursor.getString(0);
			String address = cursor.getString(1);
			String type = cursor.getString(2);
			String date = cursor.getString(3);
			serializer.startTag(null, "sms");

			serializer.startTag(null, "body");
			serializer.text(body);
			serializer.endTag(null, "body");

			serializer.startTag(null, "address");
			serializer.text(address);
			serializer.endTag(null, "address");

			serializer.startTag(null, "type");
			serializer.text(type);
			serializer.endTag(null, "type");

			serializer.startTag(null, "date");
			serializer.text(date);
			serializer.endTag(null, "date");

			serializer.endTag(null, "sms");
			// 备份过程中，增加进度
			process++;
			// pd.setProgress(process);
			callBack.onSmsBackup(process);
		}
		cursor.close();
		serializer.endTag(null, "smss");
		serializer.endDocument();
		fos.close();
	}

	/**
	 * 还原短信
	 * 
	 * @param context
	 * @param flag
	 *            是否清理原来的短信
	 */
	public static void restoreSms(Context context, boolean flag)
			throws Exception {
		Uri uri = Uri.parse("content://sms/");
		if (flag) { // true则清除本身的短信
			context.getContentResolver().delete(uri, null, null);
		}

		XmlPullParser pull = Xml.newPullParser();
		File file = new File(Environment.getExternalStorageDirectory(),
				"backup.xml");
		FileInputStream fis = new FileInputStream(file);
		pull.setInput(fis, "utf-8");
		int eventType = pull.getEventType();

		String body = null;
		String date = null;
		String type = null;
		String address = null;
		ContentValues values = null;

		while (eventType != XmlPullParser.END_DOCUMENT) {
			String tagName = pull.getName();
			switch (eventType) {
			case XmlPullParser.START_TAG: //如果是开始标签
				if("body".equals(tagName)) {
					body = pull.nextText();
				} else if("date".equals(tagName)) {
					date = pull.nextText();
				} else if("type".equals(tagName)) {
					type = pull.nextText();
				} else if("address".equals(tagName)) {
					address = pull.nextText();
				} 
				break;
			case XmlPullParser.END_TAG:
				if("sms".equals(tagName)) {
					values = new ContentValues();
					values.put("body", body);
					values.put("date", date);
					values.put("type", type);
					values.put("address", address);
					context.getContentResolver().insert(uri, values);
				}
				break;
			}
			eventType = pull.next();
		}
		fis.close();
	}

}
