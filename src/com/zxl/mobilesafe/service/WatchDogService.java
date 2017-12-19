package com.zxl.mobilesafe.service;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.zxl.mobilesafe.EnterPwdActivity;
import com.zxl.mobilesafe.db.dao.ApplockDao;

/**
 * ���Ź����� ����ϵͳ���������״̬
 * 
 * @author Administrator
 */
public class WatchDogService extends Service {
	private ActivityManager am;
	private boolean flag;
	private ApplockDao dao;
	private Intent intent;
	private InnerReceiver innerReceiver;
	private String tempStopProtectPackname;
	private ScreenOffReceiver offreceiver;
	private DataChangeReceiver dataChangeReceiver;
	private List<String> protectPacknames;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class ScreenOffReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			flag = false;
			tempStopProtectPackname = null;
		}
	}

	private class InnerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
//			System.out.println("���յ�����ʱֹͣ�����Ĺ㲥�¼�");
			tempStopProtectPackname = intent.getStringExtra("packname");
		}
	}

	private class DataChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
//			System.out.println("���ݿ�����ݱ仯�ˡ�������");
			protectPacknames = dao.findAll();
		}
	}

	@Override
	public void onCreate() {
		offreceiver = new ScreenOffReceiver();
		registerReceiver(offreceiver,
				new IntentFilter(Intent.ACTION_SCREEN_OFF));
		innerReceiver = new InnerReceiver();
		registerReceiver(innerReceiver, new IntentFilter(
				"com.zxl.mobilesafe.tempstop"));
		dataChangeReceiver = new DataChangeReceiver();
		registerReceiver(dataChangeReceiver, new IntentFilter(
				"com.zxl.mobilesafe.applockchange"));

		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		dao = new ApplockDao(this);
		protectPacknames = dao.findAll();
		flag = true;
		intent = new Intent(getApplicationContext(), EnterPwdActivity.class);
		// ������û������ջ��Ϣ�ģ��ڷ�����activity��Ҫָ�����activity���е�����ջ
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		new Thread() {
			public void run() {
				while (flag) {
					List<RunningTaskInfo> infos = am.getRunningTasks(1);
					String packname = infos.get(0).topActivity.getPackageName();
					//System.out.println("��ǰ�û�������Ӧ�ó���:" + packname);
					//if (dao.find(packname)) {//��ѯ���ݿ�̫���ˣ�������Դ���ĳɲ�ѯ�ڴ�
					if(protectPacknames.contains(packname)){//��ѯ�ڴ�Ч�ʸߺܶ�	
					    // �ж����Ӧ�ó����Ƿ���Ҫ��ʱ��ֹͣ������
						if (packname.equals(tempStopProtectPackname)) {

						} else {
							// ��ǰӦ����Ҫ�������ĳ�����������һ����������Ľ��档
							intent.putExtra("packname", packname);
							startActivity(intent);
						}
					}
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		flag = false;
		unregisterReceiver(innerReceiver);
		innerReceiver = null;
		unregisterReceiver(offreceiver);
		offreceiver = null;
		unregisterReceiver(dataChangeReceiver);
		dataChangeReceiver = null;
		super.onDestroy();
	}
}
