/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.*;

/**
 * 登录失败管理池。<br>
 * 保存不属于黑名单，但是登录时失败的账号。
 * 
 * @author scott.liang
 * @version 1.0 8/2/2018
 * @since laxcus 1.0
 */
public final class FaultOnGatePool extends VirtualPool {

	/** 登录失败管理池句柄 **/
	private static FaultOnGatePool selfHandle = new FaultOnGatePool();

	/** 超时时间 **/
	private long timeout;

	/** 用户签名 -> 登录失败 **/
	private TreeMap<User, FaultUser> sheets = new TreeMap<User, FaultUser>();

	/**
	 * 构造默认的登录失败管理池
	 */
	private FaultOnGatePool() {
		super();
		// 超时时间
		setTimeout(20 * 60000);
	}

	/**
	 * 返回登录失败管理池句柄
	 * 
	 * @return 登录失败管理池句柄
	 */
	public static FaultOnGatePool getInstance() {
		return FaultOnGatePool.selfHandle;
	}

	/**
	 * 设置超时时间
	 * @param ms 毫秒
	 */
	public void setTimeout(long ms) {
		if (ms < 10000) return;
		
		timeout = ms;
		Logger.debug(this, "setTimeout", "fault list timeout: %s ms", ms);
	}

	/**
	 * 返回登录失败记录在内存的超时时间
	 * @return
	 */
	public long getTimeout() {
		return timeout;
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
		Logger.info(this, "process", "into ...");
		while (!isInterrupted()) {
			delay(10000);
			check();
		}
		Logger.info(this, "process", "exit");
	}

	/* (non-Javadoc)
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		sheets.clear();
	}

	/**
	 * 判断黑账号存在！
	 * @param user 黑账号
	 * @return 返回真或者假
	 */
	public boolean contains(User user) {
		if (user == null) {
			return false;
		}
		boolean success = false;
		// 锁定，判断有用户名
		super.lockMulti();
		try {
			success = (sheets.get(user) != null);
		} finally {
			super.unlockMulti();
		}

		if (success) {
			Logger.error(this, "contains", "%s, On the Faultlist!", user);
		}

		return success;
	}

	/**
	 * 保存黑账号
	 * @param user 用户签名
	 */
	public void add(User user) {
		if (user == null) {
			return;
		}
		
		FaultUser item = new FaultUser(user);
		super.lockSingle();
		try {
			sheets.put(item.getUser(), item);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		Logger.error(this, "add", "%s 加入登录失败名单", user);
	}
	
	/**
	 * 删除登录失败上的一个注册用户
	 * @param siger 用户签名
	 * @return 找到并且删除返回真，否则假
	 */
	public boolean remove(User user) {
		boolean success = false;

		super.lockSingle();
		try {
			success = (sheets.remove(user) != null);
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}

		// 显示日志
		Logger.debug(this, "remove", success, "drop %s", user);

		return success;
	}

	/**
	 * 检查过期的参数
	 */
	private void check() {
		int size = sheets.size();
		if (size < 1) {
			return;
		}

		ArrayList<User> array = new ArrayList<User>(size);

		// 锁定！
		super.lockSingle();
		try {
			Iterator<Map.Entry<User, FaultUser>> iterator = sheets.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<User, FaultUser> entry = iterator.next();
				if (entry.getValue().isTimeout(timeout)) {
					array.add(entry.getKey());
				}
			}
			// 删除
			for (User user : array) {
				sheets.remove(user);
				// 删除过期
				Logger.info(this, "check", "drop %s! From the Faultlist!", user);
			}
		} catch (Throwable e) {
			Logger.fatal(e);
		} finally {
			super.unlockSingle();
		}
	}

}