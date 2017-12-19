package com.zxl.mobilesafe.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

/**
 * ϵͳ��Ϣ�Ĺ�����
 */
public class SystemInfoUtils {
	/**
	 * ��ȡ�������еĽ��̵�����
	 * @param context ������
	 * @return
	 */
	public static int getRunningProcessCount(Context context) {
		//PackageManager //�������� �൱�ڳ������������̬�����ݡ�
		//ActivityManager  ���̹�������������ֻ��Ļ��Ϣ����̬�����ݡ�
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
		return infos.size();
	}
	/**
	 * ��ȡ�ֻ����õ�ʣ���ڴ�
	 * @param context ������
	 * @return
	 */
	public static long getAvailMem(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo outInfo = new MemoryInfo();
		am.getMemoryInfo(outInfo);
		return outInfo.availMem;
	}
	/**
	 * ��ȡ�ֻ����õ����ڴ�
	 * @param context ������
	 * @return long byte
	 */
	public static long getTotalMem(Context context) {
//		//�汾4.0�Ժ�
//		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//		MemoryInfo outInfo = new MemoryInfo();
//		am.getMemoryInfo(outInfo);
//		return outInfo.totalMem;
		//���ݾɰ汾
		try {
			File file = new File("/proc/meminfo");
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = br.readLine();
			//MemTotal:         513000 kB
			StringBuilder sb = new StringBuilder();
			for(char c : line.toCharArray()) {
				if(c >= '0' && c <= '9') {
					sb.append(c);
				}
			}
			return Long.parseLong(sb.toString()) * 1024;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}














