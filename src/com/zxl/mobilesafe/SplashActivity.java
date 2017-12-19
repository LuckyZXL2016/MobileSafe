package com.zxl.mobilesafe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.zxl.mobilesafe.utils.StreamTools;

public class SplashActivity extends Activity {

	protected static final String TAG = "SplashActivity";
	protected static final int SHOW_UPDATE_DIALOG = 0;
	protected static final int ENTER_HOME = 1;
	protected static final int URL_ERROR = 2;
	protected static final int NETWORK_ERROR = 3;
	protected static final int JSON_ERROR = 4;
	private TextView tv_splash_version;
	private TextView tv_update_info;
	private String description;
	private String apkurl;
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("�汾��" + getVersionName());
		tv_update_info = (TextView) findViewById(R.id.tv_update_info);
		boolean update = sp.getBoolean("update", false);

		installShortCut();
		
		// �������ݿ�
		copyDB("address.db");
//		copyDB("commonnum.db");
		copyDB("antivirus.db");
		if (update) {
			// �������
			checkUpdate();
		} else {
			// �Զ������Ѿ��ر�
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// ������ҳ��
					enterHome();

				}
			}, 2000);
		}
		AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
		aa.setDuration(500);
		findViewById(R.id.rl_root_splash).startAnimation(aa);
	}

	/**
	 * �������ͼ��
	 */
	private void installShortCut() {
		boolean shortcut = sp.getBoolean("shortcut", false);
		if(shortcut)
			return;
		Editor editor = sp.edit();
		//���͹㲥����ͼ��׼���������ͼ��
		Intent intent = new Intent();
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		//��ݷ�ʽ  Ҫ����3����Ҫ����Ϣ 1������ 2.ͼ�� 3.��ʲô����
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "�ֻ���ʿ");
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		//������ͼ���Ӧ����ͼ��
		Intent shortcutIntent = new Intent();
		shortcutIntent.setAction("android.intent.action.MAIN");
		shortcutIntent.addCategory("android.intent.category.LAUNCHER");
		//ָ��Ҫ�򿪵ľ���Ӧ��
		shortcutIntent.setClassName(getPackageName(), "com.zxl.mobilesafe.SplashActivity");
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		sendBroadcast(intent);
		editor.putBoolean("shortcut", true);
		editor.commit();
	}

	/**
	 * �����ʲ�Ŀ¼�µ����ݿ��ļ�
	 * path ��address.db������ݿ⿽����data/data/��������/files/
	 */
	private void copyDB(String filename) {
		// ����һ�Σ��Ͳ���Ҫ�ٴο���
		try {
			File file = new File(getFilesDir(), filename);
			if (file.exists() && file.length() > 0) {
				// ����������Ҫ����
				Log.i(TAG, "�Ѵ��ڣ������ٿ���");
			} else {
				InputStream is = getAssets().open(filename);
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int len = 0;
				while((len = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
				is.close();
				fos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW_UPDATE_DIALOG:// ��ʾ�����ĶԻ���
				Log.i(TAG, "��ʾ�����ĶԻ���");
				showUpdateDialog();
				break;
			case ENTER_HOME:// ������ҳ��
				enterHome();
				break;
			case URL_ERROR: // URL����
				enterHome();
				Toast.makeText(getApplicationContext(), "URL����", 0).show();
				break;
			case NETWORK_ERROR:// �����쳣
				enterHome();
				Toast.makeText(SplashActivity.this, "�����쳣", 0).show();
				break;
			case JSON_ERROR:// JSON��������
				enterHome();
				Toast.makeText(SplashActivity.this, "JSON��������", 0).show();
				break;
			default:
				break;
			}
		}

	};

	private void checkUpdate() {

		new Thread() {

			public void run() {
				// �õ���ŵ���Ϣ
				Message mes = Message.obtain();
				long startTime = System.currentTimeMillis();
				try {
					URL url = new URL(getString(R.string.serverurl));
					// ����
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(4000);
					int code = conn.getResponseCode();
					if (code == 200) {
						// �����ɹ�
						InputStream is = conn.getInputStream();
						// ����ת��String
						String result = StreamTools.readFromStream(is);
						Log.i(TAG, "�����ɹ�" + result);
						// json����
						JSONObject obj = new JSONObject(result);
						// �õ��������İ汾��Ϣ
						String version = (String) obj.get("version");
						description = (String) obj.get("description");
						apkurl = (String) obj.get("apkurl");

						// У���Ƿ����°汾
						if (getVersionName().equals(version)) {
							// �汾һ�£�û���°汾��������ҳ��
							mes.what = ENTER_HOME;
						} else {
							// ���°汾������һ�����Ի���
							mes.what = SHOW_UPDATE_DIALOG;
						}
					}
				} catch (MalformedURLException e) {
					mes.what = URL_ERROR;
					e.printStackTrace();
				} catch (IOException e) {
					mes.what = NETWORK_ERROR;
					e.printStackTrace();
				} catch (JSONException e) {
					mes.what = JSON_ERROR;
					e.printStackTrace();
				} finally {
					long endTime = System.currentTimeMillis();
					long dTime = endTime - startTime;
					if (dTime < 2000) {
						try {
							Thread.sleep(2000 - dTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					handler.sendMessage(mes);
				}

			};
		}.start();

	}

	// ���������Ի���
	protected void showUpdateDialog() {
		AlertDialog.Builder builder = new Builder(SplashActivity.this);
		builder.setTitle("��ʾ����");
		// builder.setCancelable(false);// ǿ������
		builder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// ������ҳ��
				enterHome();
				dialog.dismiss();

			}
		});
		builder.setMessage(description);
		builder.setPositiveButton("��������", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ����APK�������滻��װ
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// sdcard����
					// afinal
					FinalHttp finalhttp = new FinalHttp();
					finalhttp.download(apkurl, Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/mobilesafeX.apk", new AjaxCallBack<File>() {

						@Override
						public void onFailure(Throwable t, int errorNo,
								String strMsg) {
							t.printStackTrace();
							Toast.makeText(getApplicationContext(), "����ʧ��", 1)
									.show();
							super.onFailure(t, errorNo, strMsg);
						}

						@Override
						public void onLoading(long count, long current) {
							super.onLoading(count, current);
							tv_update_info.setVisibility(View.VISIBLE);
							// ��ǰ���ذٷֱ�
							int progress = (int) (current * 100 / count);
							tv_update_info.setText("���ؽ��ȣ�" + progress + "%");
						}

						@Override
						public void onSuccess(File t) {
							super.onSuccess(t);
							installAPK(t);
						}

						// ��װAPK
						private void installAPK(File t) {
							Intent intent = new Intent();
							intent.setAction("android.intent.action.VIEW");
							intent.addCategory("android.intent.category.DEFAULT");
							intent.setDataAndType(Uri.fromFile(t),
									"application/vnd.android.package-archive");
							startActivity(intent);

						}

					});
				} else {
					Toast.makeText(getApplicationContext(), "û��sdcard���밲װ������",
							0).show();
					return;
				}
			}
		});
		builder.setNegativeButton("�´���˵", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				enterHome();// ������ҳ��
			}
		});
		builder.show();

	}

	protected void enterHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		// �رյ�ǰҳ��
		finish();
	}

	/**
	 * �õ�Ӧ�ó���İ汾����
	 */

	private String getVersionName() {
		// ���������ֻ���APK
		PackageManager pm = getPackageManager();

		try {
			// �õ�ָ��APK�Ĺ����嵥�ļ�
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
