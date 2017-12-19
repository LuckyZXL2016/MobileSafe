package com.zxl.mobilesafe;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zxl.mobilesafe.domain.TaskInfo;
import com.zxl.mobilesafe.engine.TaskInfoProvider;
import com.zxl.mobilesafe.utils.SystemInfoUtils;

public class TaskManagerActivity extends Activity {
	private TextView tv_process_count;
	private TextView tv_mem_info;
	private LinearLayout ll_loading;
	private ListView lv_task_manager;
	private TextView tv_status;

	private List<TaskInfo> allTaskInfos;
	private List<TaskInfo> userTaskInfos;
	private List<TaskInfo> systemTaskInfos;

	private TaskManagerAdapter adapter;

	private int processCount;
	private long availMem;
	private long totalMem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_manager);

		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		tv_mem_info = (TextView) findViewById(R.id.tv_mem_info);
		tv_process_count = (TextView) findViewById(R.id.tv_process_count);
		lv_task_manager = (ListView) findViewById(R.id.lv_task_manager);
		fillData();
		tv_status = (TextView) findViewById(R.id.tv_status);
		lv_task_manager.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (userTaskInfos != null && systemTaskInfos != null) {
					if (firstVisibleItem > userTaskInfos.size()) {
						tv_status.setText("系统进程：" + systemTaskInfos.size()
								+ "个");
					} else {
						tv_status.setText("用户进程：" + userTaskInfos.size() + "个");
					}
				}
			}
		});

		lv_task_manager.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TaskInfo taskInfo;
				if (position == 0) {// 用户进程的标签
					return;
				} else if (position == (userTaskInfos.size() + 1)) {
					return;
				} else if (position <= userTaskInfos.size()) {
					taskInfo = userTaskInfos.get(position - 1);
				} else {
					taskInfo = systemTaskInfos.get(position - 1
							- userTaskInfos.size() - 1);
				}
				if(getPackageName().equals(taskInfo.getPackname())) {
					return;
				}
				ViewHolder holder = (ViewHolder) view.getTag();
				if (taskInfo.isChecked()) {
					taskInfo.setChecked(false);
					holder.cb_status.setChecked(false);
				} else {
					taskInfo.setChecked(true);
					holder.cb_status.setChecked(true);
				}
			}
		});
	}

	private void setTitle() {
		processCount = SystemInfoUtils.getRunningProcessCount(this);
		tv_process_count.setText("运行中的进程" + processCount + "个");
		availMem = SystemInfoUtils.getAvailMem(this);
		totalMem = SystemInfoUtils.getTotalMem(this);
		tv_mem_info.setText("剩余/总内存："
				+ Formatter.formatFileSize(this, availMem) + "/"
				+ Formatter.formatFileSize(this, totalMem));
	}

	/**
	 * 填充数据
	 */
	private void fillData() {
		ll_loading.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				allTaskInfos = TaskInfoProvider
						.getTaskInfos(getApplicationContext());
				userTaskInfos = new ArrayList<TaskInfo>();
				systemTaskInfos = new ArrayList<TaskInfo>();
				for (TaskInfo info : allTaskInfos) {
					if (info.isUserTask()) {
						userTaskInfos.add(info);
					} else {
						systemTaskInfos.add(info);
					}
				}
				// 更新设置界面
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ll_loading.setVisibility(View.INVISIBLE);
						if (adapter == null) {
							adapter = new TaskManagerAdapter();
							lv_task_manager.setAdapter(adapter);
						} else {
							adapter.notifyDataSetChanged();
						}
						setTitle();
					}
				});
			};
		}.start();
	}

	private class TaskManagerAdapter extends BaseAdapter {
		// private static final String TAG = "TaskManagerAdapter";

		@Override
		public int getCount() {
			SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
			if(sp.getBoolean("showsystem", false)) {
				return userTaskInfos.size() + 1 + systemTaskInfos.size() + 1;
			} else {
				return userTaskInfos.size() + 1;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TaskInfo taskInfo;
			if (position == 0) { // 用户进程的标签
				TextView tv = new TextView(getApplicationContext());
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				tv.setText("用户进程：" + userTaskInfos.size() + "个");
				return tv;
			} else if (position == (userTaskInfos.size() + 1)) {
				TextView tv = new TextView(getApplicationContext());
				tv.setBackgroundColor(Color.GRAY);
				tv.setTextColor(Color.WHITE);
				tv.setText("系统进程：" + systemTaskInfos.size() + "个");
				return tv;
			} else if (position <= userTaskInfos.size()) {
				taskInfo = userTaskInfos.get(position - 1);
			} else {
				taskInfo = systemTaskInfos.get(position - 1
						- userTaskInfos.size() - 1);
			}
			View view;
			ViewHolder holder;
			if (convertView != null && convertView instanceof RelativeLayout) {
				view = convertView;
				holder = (ViewHolder) view.getTag();
				// Log.i(TAG, "复用缓存。。" + position);
			} else {
				view = View.inflate(getApplicationContext(),
						R.layout.list_item_taskinfo, null);
				holder = new ViewHolder();
				holder.iv_icon = (ImageView) view
						.findViewById(R.id.iv_task_icon);
				holder.tv_name = (TextView) view
						.findViewById(R.id.tv_task_name);
				holder.tv_memsize = (TextView) view
						.findViewById(R.id.tv_task_memsize);
				holder.cb_status = (CheckBox) view.findViewById(R.id.cb_status);
				view.setTag(holder);
				// Log.i(TAG, "创建新的view对象" + position);
			}
			holder.iv_icon.setImageDrawable(taskInfo.getIcon());
			holder.tv_name.setText(taskInfo.getName());
			holder.tv_memsize.setText("内存占用："
					+ Formatter.formatFileSize(getApplicationContext(),
							taskInfo.getMemsize()));
			holder.cb_status.setChecked(taskInfo.isChecked());
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
		ImageView iv_icon;
		TextView tv_name;
		TextView tv_memsize;
		CheckBox cb_status;
	}

	/**
	 * 选中全部
	 * 
	 * @param view
	 */
	public void selectAll(View view) {
		for (TaskInfo info : allTaskInfos) {
			if (getPackageName().equals(info.getPackname())) {
				continue;
			}
			info.setChecked(true);
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * 选中相反的
	 * 
	 * @param view
	 */
	public void selectOppo(View view) {
		for (TaskInfo info : allTaskInfos) {
			if (getPackageName().equals(info.getPackname())) {
				continue;
			}
			info.setChecked(!info.isChecked());
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * 一键清理
	 * 
	 * @param view
	 */
	public void killAll(View view) {
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		int count = 0;
		long savedMem = 0;
		// 记录那些被杀死的条目
		List<TaskInfo> killedTaskinfos = new ArrayList<TaskInfo>();
		for (TaskInfo info : allTaskInfos) {
			if (info.isChecked()) {// 被勾选你的，杀死这个进程
				am.killBackgroundProcesses(info.getPackname());
				if (info.isUserTask()) {
					userTaskInfos.remove(info);
				} else {
					systemTaskInfos.remove(info);
				}
				killedTaskinfos.add(info);
				count++;
				savedMem += info.getMemsize();
			}
		}
		allTaskInfos.removeAll(killedTaskinfos);
		adapter.notifyDataSetChanged();
		Toast.makeText(
				this,
				"清理了" + count + "个进程，释放了"
						+ Formatter.formatFileSize(this, savedMem) + "的内存", 1)
				.show();
		processCount -= count;
		availMem += savedMem;
		tv_process_count.setText("运行中的进程：" + processCount + "个");
		tv_mem_info.setText("剩余/总内存："
				+ Formatter.formatFileSize(this, availMem) + "/"
				+ Formatter.formatFileSize(this, totalMem));
	}
	
	/**
	 * 进入设置
	 * 
	 * @param view
	 */
	public void enterSetting(View view) {
		Intent intent = new Intent(this, TaskSettingActivity.class);
		startActivityForResult(intent, 0);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		adapter.notifyDataSetChanged();
		super.onActivityResult(requestCode, resultCode, data);
	}
}
