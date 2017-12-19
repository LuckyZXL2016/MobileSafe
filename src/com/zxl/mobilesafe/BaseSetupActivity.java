package com.zxl.mobilesafe;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public abstract class BaseSetupActivity extends Activity {

	//1.����һ������ʶ����
	private GestureDetector detector;
	
	protected SharedPreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		//2.ʵ�����������ʶ����
		detector = new GestureDetector(this, new SimpleOnGestureListener() {
			
			//�����ǵ���ָ�����滬����ʱ��ص�
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				//������x��������������
				if(Math.abs(velocityX) < 200) {
					Toast.makeText(getApplicationContext(), "������̫��", 0).show();
					return true;
				} 
				
				//����б���������
				if(Math.abs(e2.getRawY() - e1.getRawY()) > 100) {
					Toast.makeText(getApplicationContext(), "����������", 0).show();
					return true;
				}
				
				if((e2.getRawX() - e1.getRawX()) > 200) {
					//��ʾ��һ��ҳ�棺�������һ���
//					System.out.println("��ʾ��һ��ҳ�棺�������һ���");
					showPre();
					return true;
				}
				
				if((e1.getRawX() - e2.getRawX()) > 200) {
					//��ʾ��һ��ҳ�棺�������󻬶�
//					System.out.println("��ʾ��һ��ҳ�棺�������󻬶�");
					showNext();
					return true;
				}
				return super.onFling(e1, e2, velocityX, velocityY);
			}
			
		});
	}
	
	public abstract void showNext();
	public abstract void showPre();
	
	//��һ���ĵ���¼�
	public void next(View view) {
		showNext();
	}
	
	//��һ���ĵ���¼�
	public void pre(View view) {
		showPre();
	}
	
	//3.ʹ������ʶ����
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		detector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
}
