package com.zxl.mobilesafe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zxl.mobilesafe.R;

/**
 * �����Զ������Ͽؼ���������������TextView ������һ��ImageView,����һ��View
 *
 */
public class SettingClickView extends RelativeLayout {

	private TextView tv_desc;
	private TextView tv_title;
	private String desc_on;
	private String desc_off;

	//��ʼ�������ļ�
	private void iniView(Context context) {
		
		//��һ�������ļ�---�� View ���Ҽ�����SettingItemView
		View.inflate(context, R.layout.setting_click_view, this);
		tv_desc = (TextView) this.findViewById(R.id.tv_desc);
		tv_title = (TextView) this.findViewById(R.id.tv_title);
		
	}
	
	public SettingClickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		iniView(context);
	}

	/**
	 * �������������Ĺ��췽���������ļ�ʹ�õ�ʱ�����
	 * @param context
	 * @param attrs
	 */
	public SettingClickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		iniView(context);
		
		String title = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.zxl.mobilesafe", "title");
		desc_on = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.zxl.mobilesafe", "desc_on");
		desc_off = attrs.getAttributeValue("http://schemas.android.com/apk/res/com.zxl.mobilesafe", "desc_off");
		tv_title.setText(title);
		tv_desc.setText(desc_off);
	}

	public SettingClickView(Context context) {
		super(context);
		iniView(context);
	}
	
	//������Ͽؼ���������Ϣ
	public void setDesc(String text) {
		tv_desc.setText(text);
	}
	
	//������Ͽؼ��ı���
	public void setTitle(String title) {
		tv_title.setText(title);
	}
}
