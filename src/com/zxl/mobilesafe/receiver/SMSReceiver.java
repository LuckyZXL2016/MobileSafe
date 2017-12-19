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
		// д���ն��ŵĴ���
		Object[] objs = (Object[]) intent.getExtras().get("pdus");
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		
		for(Object b:objs){
			//��ȡ�������ŵĶ���
			SmsMessage sms =SmsMessage.createFromPdu((byte[]) b);
			//��ȡ���ŵķ�����
			String sender = sms.getOriginatingAddress();//15555555556
			String safenumber = sp.getString("safenumber", "");//5556
			//��ȡ��������
			String body = sms.getMessageBody();
			dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//			Toast.makeText(context, sender, 1).show();
//			Log.i(TAG, "====sender=="+sender);
			
			if(sender.contains(safenumber)){
				
				if("#*location*#".equals(body)){
					//�õ��ֻ���GPS
//					Log.i(TAG, "�õ��ֻ���GPS");
					//��������
					Intent i = new Intent(context, GPSService.class);
					context.startService(i);
					SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
					String lastlocation = sp.getString("lastlocation", null);
					if(TextUtils.isEmpty(lastlocation)){
						//λ��û�еõ�
						SmsManager.getDefault().sendTextMessage(sender, null, "geting loaction.....", null, null);
					}else{
						SmsManager.getDefault().sendTextMessage(sender, null, lastlocation, null, null);
					}
					
					
					//������㲥��ֹ��
					abortBroadcast();
				}else if("#*alarm*#".equals(body)){
					//���ű���Ӱ��
//					Log.i(TAG, "���ű���Ӱ��");
					MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
					player.setLooping(false);
					player.setVolume(1.0f, 1.0f);
					player.start();
					
					abortBroadcast();
				}
				else if("#*wipedata*#".equals(body)){
					//Զ���������
//					Log.i(TAG, "Զ���������");
//					dpm.wipeData(0);
					abortBroadcast();
				}
				else if("#*lockscreen*#".equals(body)){
					//Զ������
//					Log.i(TAG, "Զ������");
//					dpm.resetPassword("123", 0);
					dpm.lockNow();
					abortBroadcast();
				}
			}
			
		}

	}

}
