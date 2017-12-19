package com.zxl.mobilesafe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zxl.mobilesafe.db.dao.AntivirusDao;
import com.zxl.mobilesafe.utils.MD5Utils;

public class AntiVirusActivity extends Activity {
	protected static final int SCANING = 1;
	protected static final int SCAN_FINISH = 2;
	private ImageView iv_scan;
	private ProgressBar progressBar1;
	private TextView tv_scan_status;
	private LinearLayout ll_container;
	/**
	 * ������ѯ�ļ���
	 */
	private List<ScanInfo> virusInfos;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SCANING:
				ScanInfo scaninfo = (ScanInfo) msg.obj;
				tv_scan_status.setText("����ɨ�裺" + scaninfo.appname);
				TextView tv = new TextView(getApplicationContext());
				if(scaninfo.isvirus) {
					tv.setText("���ֲ�����" + scaninfo.appname);
					tv.setTextColor(Color.RED);
				} else {
					tv.setText("ɨ�谲ȫ��" + scaninfo.appname);
					tv.setTextColor(Color.BLACK);
				}
				ll_container.addView(tv, 0);
				break;
			case SCAN_FINISH:
				iv_scan.clearAnimation();
				tv_scan_status.setText("ɨ�����");
				if(virusInfos.size() > 0) { //�����˲���
					AlertDialog.Builder builder = new Builder(AntiVirusActivity.this);
					builder.setTitle("���棡����");
					builder.setMessage("�ֻ������ˣ�" + virusInfos.size() + "������,�ֻ��Ѿ�ʮ��Σ�գ��÷�0�֣��Ͻ���ɱ������");
					builder.setNegativeButton("�´���˵", null);
					builder.setPositiveButton("���̴���", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							for (ScanInfo info : virusInfos) {
								Intent intent = new Intent();
								intent.setAction(Intent.ACTION_DEFAULT);
								intent.setData(Uri.parse("package:" + info.packname));
								startActivity(intent);
							}
						}
					});
					builder.show();
				} else {
					Toast.makeText(getApplicationContext(), "�����ֻ��ǳ���ȫ����������Ŷ", 0).show();
				}
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anti_virus);
		ll_container = (LinearLayout) findViewById(R.id.ll_container);
		tv_scan_status = (TextView) findViewById(R.id.tv_scan_status);
		iv_scan = (ImageView) findViewById(R.id.iv_scan);
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		RotateAnimation ra = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		ra.setDuration(1000);
		ra.setRepeatCount(Animation.INFINITE);
		iv_scan.startAnimation(ra);

		tv_scan_status.setText("���ڳ�ʼ��ɱ������");
		virusInfos = new ArrayList<ScanInfo>();
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// ��ȡӦ�ó����������
				PackageManager pm = getPackageManager();
				// ��ȡ���е�Ӧ�ó��� ������Щ��ж�صĵ���û��ж�ظɾ� �������������ݵ�Ӧ�ã�
				List<PackageInfo> packinfos = pm
						.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES
								+ PackageManager.GET_SIGNATURES);
				progressBar1.setMax(packinfos.size());
				int progress = 0;
				Random random = new Random();
				ScanInfo scanInfo;
				for (PackageInfo packageInfo : packinfos) {
					scanInfo = new ScanInfo();
					scanInfo.packname = packageInfo.packageName;
					scanInfo.appname = packageInfo.applicationInfo
							.loadLabel(pm).toString();
					String md5 = MD5Utils.md5Password(packageInfo.signatures[0]
							.toCharsString());
					String result = AntivirusDao.isVirus(md5);
					if (result != null) {
						// ���ֲ���
						scanInfo.isvirus = true;
						scanInfo.desc = result;
						virusInfos.add(scanInfo);
					} else {
						// ɨ�谲ȫ
						scanInfo.isvirus = false;
						scanInfo.desc = null;
					}
					Message msg = Message.obtain();
					msg.what = SCANING;
					msg.obj = scanInfo;
					handler.sendMessage(msg);
					progress++;
					progressBar1.setProgress(progress);
					try {
						Thread.sleep(50 + random.nextInt(50));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Message msg = Message.obtain();
				msg.what = SCAN_FINISH;
				handler.sendMessage(msg);
			};
		}.start();
	}

	class ScanInfo {
		String packname;
		boolean isvirus;
		String desc;
		String appname;
	}
}
