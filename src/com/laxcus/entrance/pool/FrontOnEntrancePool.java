/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.entrance.pool;

import java.util.*;

import com.laxcus.command.site.gate.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * FRONT定位注册地址管理池
 * 
 * @author scott.liang
 * @version 1.0 7/19/2019
 * @since laxcus 1.0
 */
public class FrontOnEntrancePool extends VirtualPool {

	/** FRONT定位注册地址管理池 **/
	private static FrontOnEntrancePool selfHandle = new FrontOnEntrancePool();

	/** 用户签名 -> 定位结果 **/
	private Map<Siger, FrontPushItem> mapItems = new TreeMap<Siger, FrontPushItem>();
	
	/**
	 * 构造FRONT定位注册地址管理池
	 */
	private FrontOnEntrancePool() {
		super();
		setSleepTime(5000);
	}

	/**
	 * 返回FRONT定位注册地址管理池静态句柄
	 * @return 实例句柄
	 */
	public static FrontOnEntrancePool getInstance() {
		return FrontOnEntrancePool.selfHandle;
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
		Logger.debug(this, "process", "into...");
		
		while (!isInterrupted()) {
			check();
			sleep();
		}
		
		Logger.debug(this, "process", "exit!");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		mapItems.clear();
	}

	/**
	 * 保存单元
	 * @param siger 用户签名
	 * @return 成功或者否
	 */
	private boolean add(Siger siger) {
		FrontPushItem item = new FrontPushItem(siger);
		boolean success = false;
		// 锁定和保存
		super.lockSingle();
		try {
			mapItems.put(item.getSiger(), item);
			success = true;
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 删除单元
	 * @param siger 用户签名
	 * @return 返回被删除的单元，或者空指针
	 */
	private FrontPushItem remove(Siger siger) {
		super.lockSingle();
		try {
			return mapItems.remove(siger);
		} finally{
			super.unlockSingle();
		}
	}

	/**
	 * 检查和删除过期单元
	 */
	private void check() {
		int size = mapItems.size();
		if (size == 0) {
			return;
		}
		// 超时20分钟，删除！
		long timeout = 20 * 60 * 1000;
		ArrayList<Siger> a = new ArrayList<Siger>(size);
		super.lockMulti();
		try {
			Iterator<Map.Entry<Siger, FrontPushItem>> iterator = mapItems
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<Siger, FrontPushItem> entry = iterator.next();
				if (entry.getValue().isTimeout(timeout)) {
					a.add(entry.getKey());
				}
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}

		// 删除超时的单元
		for (Siger e : a) {
			remove(e);
		}
	}
	
	/**
	 * 判断有这个签名
	 * @param siger 用户签名
	 * @return 返回真或者假
	 */
	public boolean contains(Siger siger) {
		boolean success = false;
		// 锁定
		super.lockMulti();
		try {
			success = (mapItems.get(siger) != null);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/**
	 * 已经处理完毕
	 * @param siger
	 * @return
	 */
	public boolean isTouched(Siger siger) {
		boolean success = false;
		// 锁定
		super.lockMulti();
		try {
			FrontPushItem item = mapItems.get(siger);
			if (item != null) {
				success = item.isTouched();
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockMulti();
		}
		return success;
	}
	
	/**
	 * 弹出GATE站点地址
	 * @param siger 用户签名
	 * @return 返回GATE站点地址
	 */
	public Node popup(Siger siger) {
		// 从队列中删除
		FrontPushItem item = remove(siger);
		if (item != null) {
			return item.getSite();
		}
		// 返回空指针
		return null;
	}

	/**
	 * 推送和保存一个定位操作
	 * @param siger 用户签名
	 * @param wide 来自公网
	 * @return 成功返回真，否则假
	 */
	public boolean push(Siger siger, boolean wide) {
		// 保存
		add(siger);

		// 生成命令
		AssertGateUser cmd = new AssertGateUser(siger, wide);
		boolean success = EntranceCommandPool.getInstance().admit(cmd);
		// 不成功，删除它
		if (!success) {
			remove(siger);
		}

		return success;
	}

	/**
	 * 触发定位操作结果
	 * @param siger 用户签名
	 * @param gate GATE站点
	 * @return 执行成功返回真，否则假
	 */
	public boolean touch(Siger siger, Node gate) {
		boolean success = false;
		// 锁定
		super.lockSingle();
		try {
			FrontPushItem item = mapItems.get(siger);
			success = (item != null);
			if (success) {
				item.setSite(gate);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

}