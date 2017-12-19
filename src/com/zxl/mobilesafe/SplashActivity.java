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
		tv_splash_version.setText("版本号" + getVersionName());
		tv_update_info = (TextView) findViewById(R.id.tv_update_info);
		boolean update = sp.getBoolean("update", false);

		installShortCut();
		
		// 拷贝数据库
		copyDB("address.db");
//		copyDB("commonnum.db");
		copyDB("antivirus.db");
		if (update) {
			// 检查升级
			checkUpdate();
		} else {
			// 自动升级已经关闭
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// 进入主页面
					enterHome();

				}
			}, 2000);
		}
		AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
		aa.setDuration(500);
		findViewById(R.id.rl_root_splash).startAnimation(aa);
	}

	/**
	 * 创建快捷图标
	 */
	private void installShortCut() {
		boolean shortcut = sp.getBoolean("shortcut", false);
		if(shortcut)
			return;
		Editor editor = sp.edit();
		//发送广播的意图，准备创建快捷图标
		Intent intent = new Intent();
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		//快捷方式  要包含3个重要的信息 1，名称 2.图标 3.干什么事情
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "手机卫士");
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		//桌面点击图标对应的意图。
		Intent shortcutIntent = new Intent();
		shortcutIntent.setAction("android.intent.action.MAIN");
		shortcutIntent.addCategory("android.intent.category.LAUNCHER");
		//指定要打开的具体应用
		shortcutIntent.setClassName(getPackageName(), "com.zxl.mobilesafe.SplashActivity");
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		sendBroadcast(intent);
		editor.putBoolean("shortcut", true);
		editor.commit();
	}

	/**
	 * 拷贝资产目录下的数据库文件
	 * path 把address.db这个数据库拷贝到data/data/《包名》/files/
	 */
	private void copyDB(String filename) {
		// 拷贝一次，就不需要再次拷贝
		try {
			File file = new File(getFilesDir(), filename);
			if (file.exists() && file.length() > 0) {
				// 正常，不需要拷贝
				Log.i(TAG, "已存在，不需再拷贝");
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
			case SHOW_UPDATE_DIALOG:// 显示升级的对话框
				Log.i(TAG, "显示升级的对话框");
				showUpdateDialog();
				break;
			case ENTER_HOME:// 进入主页面
				enterHome();
				break;
			case URL_ERROR: // URL错误
				enterHome();
				Toast.makeText(getApplicationContext(), "URL错误", 0).show();
				break;
			case NETWORK_ERROR:// 网络异常
				enterHome();
				Toast.makeText(SplashActivity.this, "网络异常", 0).show();
				break;
			case JSON_ERROR:// JSON解析出错
				enterHome();
				Toast.makeText(SplashActivity.this, "JSON解析出错", 0).show();
				break;
			default:
				break;
			}
		}

	};

	private void checkUpdate() {

		new Thread() {

			public void run() {
				// 得到存放的信息
				Message mes = Message.obtain();
				long startTime = System.currentTimeMillis();
				try {
					URL url = new URL(getString(R.string.serverurl));
					// 联网
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(4000);
					int code = conn.getResponseCode();
					if (code == 200) {
						// 联网成功
						InputStream is = conn.getInputStream();
						// 把流转成String
						String result = StreamTools.readFromStream(is);
						Log.i(TAG, "联网成功" + result);
						// json解析
						JSONObject obj = new JSONObject(result);
						// 得到服务器的版本信息
						String version = (String) obj.get("version");
						description = (String) obj.get("description");
						apkurl = (String) obj.get("apkurl");

						// 校验是否有新版本
						if (getVersionName().equals(version)) {
							// 版本一致，没有新版本，进入主页面
							mes.what = ENTER_HOME;
						} else {
							// 有新版本，弹出一升级对话框
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

	// 弹出升级对话框
	protected void showUpdateDialog() {
		AlertDialog.Builder builder = new Builder(SplashActivity.this);
		builder.setTitle("提示升级");
		// builder.setCancelable(false);// 强制升级
		builder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// 进入主页面
				enterHome();
				dialog.dismiss();

			}
		});
		builder.setMessage(description);
		builder.setPositiveButton("立刻升级", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 下载APK，并且替换安装
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// sdcard存在
					// afinal
					FinalHttp finalhttp = new FinalHttp();
					finalhttp.download(apkurl, Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/mobilesafeX.apk", new AjaxCallBack<File>() {

						@Override
						public void onFailure(Throwable t, int errorNo,
								String strMsg) {
							t.printStackTrace();
							Toast.makeText(getApplicationContext(), "下载失败", 1)
									.show();
							super.onFailure(t, errorNo, strMsg);
						}

						@Override
						public void onLoading(long count, long current) {
							super.onLoading(count, current);
							tv_update_info.setVisibility(View.VISIBLE);
							// 当前下载百分比
							int progress = (int) (current * 100 / count);
							tv_update_info.setText("下载进度：" + progress + "%");
						}

						@Override
						public void onSuccess(File t) {
							super.onSuccess(t);
							installAPK(t);
						}

						// 安装APK
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
					Toast.makeText(getApplicationContext(), "没有sdcard，请安装上再试",
							0).show();
					return;
				}
			}
		});
		builder.setNegativeButton("下次再说", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				enterHome();// 进入主页面
			}
		});
		builder.show();

	}

	protected void enterHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		// 关闭当前页面
		finish();
	}

	/**
	 * 得到应用程序的版本名称
	 */

	private String getVersionName() {
		// 用来管理手机的APK
		PackageManager pm = getPackageManager();

		try {
			// 得到指定APK的功能清单文件
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
