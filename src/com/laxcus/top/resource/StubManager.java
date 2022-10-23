/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.resource;

import java.io.*;

import com.laxcus.command.reserve.*;
import com.laxcus.pool.schedule.*;
import com.laxcus.top.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.lock.*;

/** 
 * 数据块编号管理器。<br>
 * 数据块编号的范围在 Long.MIN_VALUE - Long.MAX_VALUE之间，0是无效值。分配从最小值开始，逐一增加。<br>
 * 
 * @author scott.liang
 * @version 1.01 11/17/2013
 * @since laxcus 1.0
 */
public final class StubManager extends MutexHandler {

	/** 数据块编号管理器静态实例 **/
	private static StubManager selfHandle = new StubManager();

	/** 磁盘文件名 **/
	private final static String filename = "stubs.conf";

	/** 数据块编号刻度，从最小开始 **/
	private long stubScale = Long.MIN_VALUE;

	/**
	 * 建立默认的数据块编号管理器。
	 */
	private StubManager() {
		super();
	}

	/**
	 * 生成磁盘文件
	 * @return
	 */
	private File getFile() {
		return TopLauncher.getInstance().createResourceFile(StubManager.filename);
	}

	/**
	 * 返回数据块编号管理器的静态实例。
	 * @return
	 */
	public static StubManager getInstance() {
		return StubManager.selfHandle;
	}

	/**
	 * 返回当前的数据块编号刻度位置
	 * @return
	 */
	public long getCurrentScale() {
		return stubScale;
	}

	/**
	 * 分配一段数据块编号
	 * @param size - 编号数目
	 * @return - 返回长整型数组
	 */
	public long[] allocate(int size) {
		long[] stubs = new long[size];

		// 分配一个资源名称，锁定它
		DefaultSerialSchedule task = new DefaultSerialSchedule("ALLOCATE_STUB");
		task.lock();

		for (int i = 0; i < stubs.length; i++) {
			stubs[i] = stubScale++;
			if (stubScale == 0L) {
				stubScale++;
			}
		}

		// 输入到磁盘
		this.flush();
		// 解除锁定
		task.unlock();

		// 通知TOP监视站点更新
		File file = getFile();
		DispatchReserveResource cmd = new DispatchReserveResource(file.getAbsolutePath());
		TopCommandPool.getInstance().admit(cmd);

		return stubs;
	}

	/**
	 * 从磁盘上加载数据块编号刻度
	 */
	public boolean load() {
		File file = getFile();
		// 允许数据块配置文件不存在
		if (!file.exists()) {
			return true;
		}
		byte[] b = TopLauncher.getInstance().readFile(file);

		ClassReader reader = new ClassReader(b);
		stubScale = reader.readLong();
		return true;
	}

	/**
	 * 把最新的数据块刻度输出到磁盘上
	 * @return - 成功返回真，否则假
	 */
	private boolean flush() {
		File file = getFile();
		ClassWriter writer = new ClassWriter();
		writer.writeLong(stubScale);
		byte[] b = writer.effuse();
		return TopLauncher.getInstance().flushFile(file, b);
	}
}