/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool.schedule;

import java.util.*;

import com.laxcus.log.client.*;
import com.laxcus.pool.*;

/**
 * 串行业务管理池 <br>
 * 
 * 在一个站点中运行，管理这个站点全部资源的串行处理业务。
 * 
 * @author scott.liang
 * @version 1.1 7/12/2015
 * @since laxcus 1.0
 */
public final class SerialSchedulePool extends VirtualPool {

	/** 串行业务管理池句柄 **/
	private static SerialSchedulePool selfHandle = new SerialSchedulePool();

	/** 串行业务管理器 **/
	private Map<String, SerialScheduleManager> array = new TreeMap<String, SerialScheduleManager>();

	/**
	 * 构造串行业务管理池
	 */
	private SerialSchedulePool() {
		super();
	}

	/**
	 * 返回串行业务管理池句柄
	 * @return
	 */
	public static SerialSchedulePool getInstance() {
		return SerialSchedulePool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info(this, "process", "into...");
		while (!isInterrupted()) {
			super.sleep();
		}
		Logger.info(this, "process", "exit...");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// 锁定
		super.lockSingle();
		try {
			Iterator<Map.Entry<String, SerialScheduleManager>> iterator = array.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, SerialScheduleManager> entry = iterator.next();
				SerialScheduleManager e = entry.getValue();
				e.wakeupAll(); // 退出前唤醒全部！
			}
			// 清除
			array.clear();
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * 提交一个基于资源名称的串行业务
	 * @param resource 资源名称
	 * @param schedule 串行处理业务
	 * @return 提交且绑定成功，返回真；否则假。
	 */
	public boolean admit(String resource, SerialSchedule schedule) {
		boolean success = false;
		super.lockSingle();
		try {
			SerialScheduleManager manager = array.get(resource);
			if (manager == null) {
				manager = new SerialScheduleManager(resource);
				array.put(manager.getResource(), manager);
			}
			// 提交参数
			success = manager.attach(schedule);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "admit", success, "%s", resource);

		return success;
	}

	/**
	 * 释放当前实例，并且启动下一个
	 * @param resource 资源名称
	 * @param schedule 串行处理业务
	 * @return 成功返回“真”，否则“假”。
	 */
	public boolean release(String resource, SerialSchedule schedule) {
		boolean success = false;
		super.lockSingle();
		try {
			SerialScheduleManager manager = array.get(resource);
			success = (manager != null);
			if (success) {
				success = manager.detach(schedule);
			}
			// 申请下一个
			if (success) {
				manager.next();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.debug(this, "release", success, "remove %s", resource);

		return success;
	}

	/**
	 * 判断一个串行处理业务处于运行状态
	 * @param resource 资源名称
	 * @param schedule 串行处理业务
	 * @return 返回真或者假
	 */
	public boolean isRunning(String resource, SerialSchedule schedule) {
		boolean success = false;
		super.lockMulti();
		try {
			SerialScheduleManager manager = array.get(resource);
			success = (manager != null);
			if (success) {
				success = manager.isRunning(schedule);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "isRunning", success, "%s", resource);

		return success;
	}

	/**
	 * 判断一个串行处理业务存在
	 * @param resource 资源名称
	 * @param schedule 串行处理业务
	 * @return 返回真或者假
	 */
	public boolean contains(String resource, SerialSchedule schedule) {
		boolean success = false;
		super.lockMulti();
		try {
			SerialScheduleManager manager = array.get(resource);
			success = (manager != null);
			if (success) {
				success = manager.contains(schedule);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		Logger.debug(this, "contains", success, "%s", resource);

		return success;
	}

}