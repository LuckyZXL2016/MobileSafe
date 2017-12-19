package com.zxl.mobilesafe.test;

import java.util.List;

import android.test.AndroidTestCase;

import com.zxl.mobilesafe.domain.TaskInfo;
import com.zxl.mobilesafe.engine.TaskInfoProvider;

public class TestTaskInfoProvider extends AndroidTestCase {
	public void testGetTaskInfos() throws Exception{
		List<TaskInfo> infos = TaskInfoProvider.getTaskInfos(getContext());
		for(TaskInfo info:infos){
			System.out.println(info.toString());
		}
	}
}
