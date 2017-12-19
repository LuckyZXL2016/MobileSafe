package com.zxl.mobilesafe.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceUtils {

	/**
	 * 检验某个服务的存活
	 * @param context
	 * @param serviceName  传进服务的名称
	 * @return
	 */
	public static boolean isServiceRunning(Context context, String serviceName) {
		//校验服务是否存活
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> infos = am.getRunningServices(100);
		for (RunningServiceInfo info : infos) {
			String name = info.service.getClassName();
			if(serviceName.equals(name)) {
				return true;
			}
		}
		return false;
	}
}
