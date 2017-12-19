package com.zxl.mobilesafe.receiver;

import com.zxl.mobilesafe.R;
import com.zxl.mobilesafe.service.GPSService;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

	private static final String TAG = "SMSReceiver";
	private SharedPreferences sp;
	private DevicePolicyManager dpm;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// 写接收短信的代码
		Object[] objs = (Object[]) intent.getExtras().get("pdus");
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		
		for(Object b:objs){
			//获取这条短信的对象
			SmsMessage sms =SmsMessage.createFromPdu((byte[]) b);
			//获取短信的发件人
			String sender = sms.getOriginatingAddress();//15555555556
			String safenumber = sp.getString("safenumber", "");//5556
			//获取短信内容
			String body = sms.getMessageBody();
			dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//			Toast.makeText(context, sender, 1).show();
//			Log.i(TAG, "====sender=="+sender);
			
			if(sender.contains(safenumber)){
				
				if("#*location*#".equals(body)){
					//得到手机的GPS
//					Log.i(TAG, "得到手机的GPS");
					//启动服务
					Intent i = new Intent(context, GPSService.class);
					context.startService(i);
					SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
					String lastlocation = sp.getString("lastlocation", null);
					if(TextUtils.isEmpty(lastlocation)){
						//位置没有得到
						SmsManager.getDefault().sendTextMessage(sender, null, "geting loaction.....", null, null);
					}else{
						SmsManager.getDefault().sendTextMessage(sender, null, lastlocation, null, null);
					}
					
					
					//把这个广播终止掉
					abortBroadcast();
				}else if("#*alarm*#".equals(body)){
					//播放报警影音
//					Log.i(TAG, "播放报警影音");
					MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
					player.setLooping(false);
					player.setVolume(1.0f, 1.0f);
					player.start();
					
					abortBroadcast();
				}
				else if("#*wipedata*#".equals(body)){
					//远程清除数据
//					Log.i(TAG, "远程清除数据");
//					dpm.wipeData(0);
					abortBroadcast();
				}
				else if("#*lockscreen*#".equals(body)){
					//远程锁屏
//					Log.i(TAG, "远程锁屏");
//					dpm.resetPassword("123", 0);
					dpm.lockNow();
					abortBroadcast();
				}
			}
			
		}

	}

}
