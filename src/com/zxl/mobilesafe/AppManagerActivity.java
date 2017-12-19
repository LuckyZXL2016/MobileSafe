package com.zxl.mobilesafe;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zxl.mobilesafe.db.dao.ApplockDao;
import com.zxl.mobilesafe.domain.AppInfo;
import com.zxl.mobilesafe.engine.AppInfoProvider;
import com.zxl.mobilesafe.utils.DensityUtil;

public class AppManagerActivity extends Activity implements OnClickListener {
	private static final String TAG = "AppManagerActivity";
	private TextView tv_avail_sd;
	private TextView tv_avail_rom;

	private ListView lv_app_manager;
	private LinearLayout ll_loading;

	/**
	 * ���е�Ӧ�ó������Ϣ
	 */
	private List<AppInfo> appInfos;

	/**
	 * �û�Ӧ�ó���ļ���
	 */
	private List<AppInfo> userAppInfos;

	/**
	 * ��ǰ������Ϣ��״̬��
	 */
	private TextView tv_status;

	/**
	 * ������������
	 */
	private PopupWindow popupWindow;

	/**
	 * ϵͳӦ�ó���ļ���
	 */
	private List<AppInfo> systemAppInfos;

	/**
	 * ����
	 */
	private LinearLayout ll_start;
	/**
	 * ����
	 */
	private LinearLayout ll_share;
	/**
	 * ж��
	 */
	private LinearLayout ll_uninstall;

	/**
	 * ���������Ŀ��
	 */
	private AppInfo appInfo;

	private AppManagerAdapter adapter;
	
	private ApplockDao dao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		dao = new ApplockDao(this);
		tv_avail_sd = (TextView) findViewById(R.id.tv_avail_sd);
		tv_avail_rom = (TextView) findViewById(R.id.tv_avail_rom);
		tv_status = (TextView) findViewById(R.id.tv_status);
		long sdsize = getAvailSpace(Environment.getExternalStorageDirectory()
				.getAbsolutePath());
		long romsize = getAvailSpace(Environment.getDataDirectory()
				.getAbsolutePath());
		tv_avail_sd
				.setText("SD�����ÿռ䣺" + Formatter.formatFileSize(this, sdsize));
		tv_avail_rom.setText("�ڴ���ÿռ䣺"
				+ Formatter.formatFileSize(this, romsize));

		lv_app_manager = (ListView) findViewById(R.id.lv_app_manager);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);

		fillData();

		// ��listviewע��һ�������ļ�����
		lv_app_manager.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			// ������ʱ����õķ�����
			// firstVisibleItem ��һ���ɼ���Ŀ��listview���������λ�á�
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (userAppInfos != null && systemAppInfos != null) {
					if (firstVisibleItem > userAppInfos.size()) {
						tv_status.setText("ϵͳ����" + systemAppInfos.size() + "��");
					} else {
						tv_status.setText("�û�����" + userAppInfos.size() + "��");
					}
				}
			}
		});

		/**
		 * ����listview�ĵ���¼�
		 */
		lv_app_manager.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					return;
				} else if (position == (userAppInfos.size() + 1)) {
					return;
				} else if (position <= userAppInfos.size()) {// �û�����
					int newposition = position - 1;
					appInfo = userAppInfos.get(newposition);
				} else {// ϵͳ����
					int newposition = position - 1 - userAppInfos.size() - 1;
					appInfo = systemAppInfos.get(newposition);
				}
				dismissPopupWindow();
				// TextView contentView = new TextView(getApplicationContext());
				// contentView.setText(appInfo.getPackname());
				// contentView.setTextColor(Color.BLACK);
				// popupWindow.setBackgroundDrawable(new
				// ColorDrawable(Color.RED));
				View contentView = View.inflate(getApplicationContext(),
						R.layout.popup_app_item, null);
				ll_start = (LinearLayout) contentView
						.findViewById(R.id.ll_start);
				ll_share = (LinearLayout) contentView
						.findViewById(R.id.ll_share);
				ll_uninstall = (LinearLayout) contentView
						.findViewById(R.id.ll_uninstall);

				ll_start.setOnClickListener(AppManagerActivity.this);
				ll_share.setOnClickListener(AppManagerActivity.this);
				ll_uninstall.setOnClickListener(AppManagerActivity.this);

				popupWindow = new PopupWindow(contentView, -2, -2);
				// ����Ч���Ĳ��ű���Ҫ�����б�����ɫ
				// ͸����ɫҲ����ɫ
				popupWindow.setBackgroundDrawable(new ColorDrawable(
						Color.TRANSPARENT));
				int[] location = new int[2];
				view.getLocationInWindow(location);
				// �ڴ����������õĿ��ֵ ��������---��dip
				int dip = 60;
				int px = DensityUtil.dip2px(getApplicationContext(), dip);
				popupWindow.showAtLocation(parent, Gravity.LEFT | Gravity.TOP,
						px, location[1]);
				ScaleAnimation sa = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f,
						Animation.RELATIVE_TO_SELF, 0,
						Animation.RELATIVE_TO_SELF, 0.5f);
				sa.setDuration(300);
				AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
				aa.setDuration(300);
				AnimationSet set = new AnimationSet(false);
				set.addAnimation(aa);
				set.addAnimation(sa);
				contentView.startAnimation(set);
			}
		});
		
		//������  ������Ŀ��������¼�������
		lv_app_manager.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					return true;
				} else if (position == (userAppInfos.size() + 1)) {
					return true;
				} else if (position <= userAppInfos.size()) {// �û�����
					int newposition = position - 1;
					appInfo = userAppInfos.get(newposition);
				} else {// ϵͳ����
					int newposition = position - 1 - userAppInfos.size() - 1;
					appInfo = systemAppInfos.get(newposition);
				}
				ViewHolder holder = (ViewHolder) view.getTag();
				//�ж���Ŀ�Ƿ�����ڳ��������ݿ�����
				if (dao.find(appInfo.getPackname())) {
					//�������ĳ��򣬽�����������½���Ϊ�򿪵�С��ͼƬ
					dao.delete(appInfo.getPackname());
					holder.iv_status.setImageResource(R.drawable.unlock);
				} else {
					//�������򣬸��½���Ϊ�رյ���
					dao.add(appInfo.getPackname());
					holder.iv_status.setImageResource(R.drawable.lock);
				}
				return true;
			}
		});
	}

	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				appInfos = AppInfoProvider.getAppInfos(AppManagerActivity.this);
				userAppInfos = new ArrayList<AppInfo>();
				systemAppInfos = new ArrayList<AppInfo>();
				for (AppInfo info : appInfos) {
					if (info.isUserApp()) {
						userAppInfos.add(info);
					} else {
						systemAppInfos.add(info);
					}
				}
				// ����listview������������
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (adapter == null) {
							adapter = new AppManagerAdapter();
							lv_app_manager.setAdapter(adapter);
						} else {
							adapter.notifyDataSetChanged();
						}
						ll_loading.setVisibility(View.INVISIBLE);
					}
				});
			};
		}.start();
	}

	private class AppManagerAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// return appInfos.size();
			return userAppInfos.size() + 1 + systemAppInfos.size() + 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AppInfo appInfo;
			if (position == 0) {// ��ʾ�����û������ж��ٸ���С��ǩ
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.GRAY);
				tv.setText("�û�����" + userAppInfos.size() + "��");
				return tv;
			} else if (position == (userAppInfos.size() + 1)) {
				TextView tv = new TextView(getApplicationContext());
				tv.setTextColor(Color.WHITE);
				tv.setBackgroundColor(Color.GRAY);
				tv.setText("ϵͳ����" + systemAppInfos.size() + "��");
				return tv;
			} else if (position <= userAppInfos.size()) {// �û�����
				int newposition = position - 1;// ��Ϊ����һ��textview���ı�ռ����λ��
				appInfo = userAppInfos.get(newposition);
			} else {// ϵͳ����
				int newposition = position - 1 - userAppInfos.size() - 1;
				appInfo = systemAppInfos.get(newposition);
			}
			View view;
			ViewHolder holder;
			// if(position < userAppInfos.size()) {//��Щλ���������û�������ʾ��
			// appInfo = userAppInfos.get(position);
			// } else {//��Щλ��������ϵͳ�����
			// int newposition = position - userAppInfos.size();
			// appInfo = systemAppInfos.get(newposition);
			// }
			if (convertView != null && convertView instanceof RelativeLayout) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			} else {
				view = View.inflate(getApplicationContext(),
						R.layout.list_item_appinfo, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view
						.findViewById(R.id.iv_app_icon);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
				holder.tv_location = (TextView) view
						.findViewById(R.id.tv_app_location);
				holder.iv_status = (ImageView) view.findViewById(R.id.iv_status);
				view.setTag(holder);
			}

			holder.iv_icon.setImageDrawable(appInfo.getIcon());
			holder.tv_name.setText(appInfo.getName());
			if (appInfo.isInRom()) {
				holder.tv_location.setText("�ֻ��ڴ�");
			} else {
				holder.tv_location.setText("�ⲿ�洢");
			}
			if (dao.find(appInfo.getPackname())) {
				holder.iv_status.setImageResource(R.drawable.lock);
			} else {
				holder.iv_status.setImageResource(R.drawable.unlock);
			}
			return view;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

	static class ViewHolder {
		TextView tv_name;
		TextView tv_location;
		ImageView iv_icon;
		ImageView iv_status;
	}

	/**
	 * ��ȡĳ��Ŀ¼�Ŀ��ÿռ�
	 * 
	 * @param path
	 * @return
	 */
	private long getAvailSpace(String path) {
		StatFs statf = new StatFs(path);
		statf.getBlockCount();// ��ȡ�����ĸ���
		int size = statf.getBlockSize();// ��ȡ�����Ĵ�С
		int count = statf.getAvailableBlocks();// ��ȡ���õ�����ĸ���
		return size * count;
	}

	private void dismissPopupWindow() {
		// �Ѿɵĵ�������رյ�
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}

	@Override
	protected void onDestroy() {
		dismissPopupWindow();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		dismissPopupWindow();
		switch (v.getId()) {
		case R.id.ll_start:
//			Log.i(TAG, "������" + appInfo.getName());
			startApplication();
			break;
		case R.id.ll_share:
//			Log.i(TAG, "����" + appInfo.getName());
			shareApplication();
			break;
		case R.id.ll_uninstall:
			if (appInfo.isUserApp()) {
//				Log.i(TAG, "ж�أ�" + appInfo.getName());
				uninstallApplication();
			} else {
				Toast.makeText(this, "��Ҫ��ȡrootȨ��", 0).show();
			}
			break;
		}
	}

	/**
	 * ����һ��Ӧ�ó���
	 */
	private void shareApplication() {
		// Intent { act=android.intent.action.SEND typ=text/plain flg=0x3000000 cmp=com.android.mms/.ui.ComposeMessageActivity (has extras) } from pid 256
		Intent intent = new Intent();
		intent.setAction("android.intent.action.SEND");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, "�Ƽ���ʹ��һ�����,���ƽУ�"+appInfo.getName());
		startActivity(intent);
	}

	private void uninstallApplication() {
		// <action android:name="android.intent.action.VIEW" />
		// <action android:name="android.intent.action.DELETE" />
		// <category android:name="android.intent.category.DEFAULT" />
		// <data android:scheme="package" />
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:" + appInfo.getPackname()));
		startActivityForResult(intent, 0);
	}

	
	/**
	 * ж��Ӧ��
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// ˢ�½���
		fillData();
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ����һ��Ӧ�ó���
	 */
	private void startApplication() {
		// ��ѯ���Ӧ�ó�������activity��������������
		PackageManager pm = getPackageManager();
		// Intent intent = new Intent();
		// intent.setAction("android.intent.action.MAIN");
		// intent.addCategory("android.intent.category.LAUNCHER");
		// //��ѯ�������е��ֻ��Ͼ�������������activity
		// List<ResolveInfo> infos = pm.queryIntentActivities(intent,
		// PackageManager.GET_INTENT_FILTERS);
		Intent intent = pm.getLaunchIntentForPackage(appInfo.getPackname());
		if (intent != null) {
			startActivity(intent);
		} else {
			Toast.makeText(this, "����������ǰӦ��", 0).show();
		}
	}
}
