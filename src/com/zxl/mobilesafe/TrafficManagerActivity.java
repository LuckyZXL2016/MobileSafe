package com.zxl.mobilesafe;

import java.util.List;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;

public class TrafficManagerActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//1.��ȡһ����������
		PackageManager pm = getPackageManager();
		//2.�����ֻ�����ϵͳ��ȡ���е�Ӧ�ó����id
		List<ApplicationInfo> applicationInfos = pm.getInstalledApplications(0);
		for (ApplicationInfo applicationInfo : applicationInfos) {
			int uid = applicationInfo.uid;
			long tx = TrafficStats.getUidTxBytes(uid);//���͵�  �ϴ�������byte
			long rx = TrafficStats.getUidRxBytes(uid);//���ص�����byte
			//��������ֵ-1 �������Ӧ�ó���û�в������� ���߲���ϵͳ��֧������ͳ��
		}
		// TrafficStats.getMobileTxBytes(); //3g/2g�����ϴ���������
		// TrafficStats.getMobileRxBytes();//3g/2g�������ص�������
		// TrafficStats.getTotalRxBytes();// �ֻ��� + wifi
		// TrafficStats.getTotalTxBytes();
		// TrafficStats.getUidRxBytes(uid);
		// TrafficStats.getUidTxBytes(uid);
		setContentView(R.layout.activity_traffic_manager);
	}
}
