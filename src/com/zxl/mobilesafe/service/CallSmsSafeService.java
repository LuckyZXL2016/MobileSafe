package com.zxl.mobilesafe.service;

import java.lang.reflect.Method;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.zxl.mobilesafe.db.dao.BlackNumberDao;

public class CallSmsSafeService extends Service {

	public static final String TAG = "CallSmsSafeService";
	private BlackNumberDao dao;
	private InnerSmsReceiver receiver;
	private TelephonyManager tm;
	private MyListener listener;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class InnerSmsReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
//			Log.i(TAG, "内部广播接受者， 短信到来");
			//检查发件人是否是黑名单号码，设置短信拦截全部拦截
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for (Object obj : objs) {
				//具体的某一条短信
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
				//得到短信发件人
				String sender = smsMessage.getOriginatingAddress();
				String result = dao.findMode(sender);
				if("2".equals(result) || "3".equals(result)) {
//					Log.i(TAG, "拦截短信");
					abortBroadcast();
				}
			}
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		dao = new BlackNumberDao(this);
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyListener();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		receiver = new InnerSmsReceiver();
		IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		registerReceiver(receiver, filter);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		receiver = null;
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
	}
	
	private class MyListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING://零响状态。
				String result = dao.findMode(incomingNumber);
				if("1".equals(result)||"3".equals(result)){
//					Log.i(TAG,"挂断电话。。。。");
					//观察呼叫记录数据库内容的变化
					Uri uri = Uri.parse("content://call_log/calls");
					getContentResolver().registerContentObserver(uri, true, new CallLogObserver(incomingNumber, new Handler()));
					endCall();//另外一个进程里面运行的 远程服务的方法。 方法调用后，呼叫记录可能还没有生成。
					//删除呼叫记录
					//另外一个应用程序联系人的应用的私有数据库
//					deleteCallLog(incomingNumber);
				}
				break;
			}
		}
	}
	
	private class CallLogObserver extends ContentObserver {
		private String incomingNumber;
		
		public CallLogObserver(String incomingNumber, Handler handler) {
			super(handler);
			this.incomingNumber = incomingNumber;
		}

		@Override
		public void onChange(boolean selfChange) {
//			Log.i(TAG, "数据库的内容变化了，产生呼叫记录");
			getContentResolver().unregisterContentObserver(this);
			deleteCallLog(incomingNumber);
			super.onChange(selfChange);
		}
		
	}

	public void endCall() {
		//IBinder iBinder = ServiceManager.getService(TELEPHONY_SERVICE);
		try {
			//加载servicemanager的字节码
			Class clazz = CallSmsSafeService.class.getClassLoader().loadClass("android.os.ServiceManager");
			Method method = clazz.getDeclaredMethod("getService", String.class);
			IBinder ibinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
			ITelephony.Stub.asInterface(ibinder).endCall();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 利用内容提供者删除呼叫记录
	 * @param incomingNumber
	 */
	public void deleteCallLog(String incomingNumber) {
		ContentResolver resolver = getContentResolver();
		//呼叫记录uri的路径
		Uri uri = Uri.parse("content://call_log/calls");
		resolver.delete(uri, "number=?", new String[]{incomingNumber});
	}
}
