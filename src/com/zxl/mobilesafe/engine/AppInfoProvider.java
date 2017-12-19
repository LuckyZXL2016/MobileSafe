package com.zxl.mobilesafe.engine;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.zxl.mobilesafe.domain.AppInfo;

/**
 * ҵ�񷽷����ṩ�ֻ����氲װ�����е�Ӧ�ó�����Ϣ
 */
public class AppInfoProvider {
	
	/**
	 * ��ȡ���еİ�װ��Ӧ�ó�����Ϣ��
	 * @param context ������
	 * @return
	 */
	public static List<AppInfo> getAppInfos(Context context) {
		PackageManager pm = context.getPackageManager();
		//���еİ�װ��ϵͳ�ϵ�Ӧ�ó������Ϣ
		List<PackageInfo> packInfos = pm.getInstalledPackages(0);
		List<AppInfo> appInfos = new ArrayList<AppInfo>();
		for (PackageInfo packInfo : packInfos) {
			AppInfo appInfo = new AppInfo();
			//packInfo �൱��һ��Ӧ�ó���apk�����嵥�ļ�
			String packname = packInfo.packageName;
			Drawable icon = packInfo.applicationInfo.loadIcon(pm);
			String name = packInfo.applicationInfo.loadLabel(pm).toString();
			int flags = packInfo.applicationInfo.flags;//Ӧ�ó�����Ϣ�ı�ǣ��൱���û��ύ�Ĵ��
			int uid = packInfo.applicationInfo.uid;//����ϵͳ�����Ӧ��ϵͳ��һ���̶��ı�š�һ��Ӧ�ó���װ���ֻ�id�͹̶�������
			appInfo.setUid(uid);
			if((flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				//�û�����
				appInfo.setUserApp(true);
			} else {
				//ϵͳ����
				appInfo.setUserApp(false);
			}
			if((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {
				//�ֻ����ڴ�
				appInfo.setInRom(true);
			} else {
				//�ֻ���洢�豸
				appInfo.setInRom(false);
			}
			appInfo.setPackname(packname);
			appInfo.setIcon(icon);
			appInfo.setName(name);
			appInfos.add(appInfo);
		}
		return appInfos;
	}
}
