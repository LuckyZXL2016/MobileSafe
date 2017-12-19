package com.zxl.mobilesafe.test;

import java.util.List;
import java.util.Random;

import android.test.AndroidTestCase;

import com.zxl.mobilesafe.db.BlackNumberDBOpenHelper;
import com.zxl.mobilesafe.db.dao.BlackNumberDao;
import com.zxl.mobilesafe.domain.BlackNumberInfo;

public class TestBlackNumberDB extends AndroidTestCase {
	public void testCreateDB() throws Exception {
		BlackNumberDBOpenHelper helper = new BlackNumberDBOpenHelper(getContext());
		helper.getWritableDatabase();
	}
	
	public void testAdd() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		long basenumber = 13500000000l;
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			dao.add(String.valueOf(basenumber+i), String.valueOf(random.nextInt(3)+1));
		}
	}
	
	public void testFindAll() throws Exception{
		BlackNumberDao dao = new BlackNumberDao(getContext());
		List<BlackNumberInfo> infos = dao.findAll();
		for(BlackNumberInfo info:infos){
			System.out.println(info.toString());
		}
	}

	public void testDelete() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		dao.delete("110");
	}

	public void testUpdate() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		dao.update("110", "2");
	}

	public void testFind() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		boolean result = dao.find("110");
		assertEquals(true, result);
	}
}
