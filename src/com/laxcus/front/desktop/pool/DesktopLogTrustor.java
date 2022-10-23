/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.pool;

import java.util.*;

import com.laxcus.front.desktop.*;
import com.laxcus.gui.log.*;
import com.laxcus.util.display.*;
import com.laxcus.util.lock.*;

/**
 * Desktop节点日志代理
 * 
 * @author scott.liang
 * @version 1.0 5/19/2021
 * @since laxcus 1.0
 */
public final class DesktopLogTrustor extends DisplayLogTrustor {

	/** Desktop节点日志代理实例 **/
	private static DesktopLogTrustor selfHandle = new DesktopLogTrustor();

	/** 单向锁 **/
	private SingleLock lock = new SingleLock();

	/** 日志打印机 **/
	private LogTransmitter transmitter;

	/**
	 * 构造默认的Desktop节点日志代理
	 */
	private DesktopLogTrustor() {
		super();
		setSleepTime(5);
	}

	/**
	 * 返回Desktop节点日志代理句柄实例
	 * @return DesktopLogTrustor句柄
	 */
	public static DesktopLogTrustor getInstance() {
		return DesktopLogTrustor.selfHandle;
	}

	/**
	 * 设置日志转发器
	 * @param e 日志转发器
	 */
	public void setLogTransmitter(LogTransmitter e) {
		lock.lock();
		try {
			transmitter = e;
		} catch (Throwable ex) {

		} finally {
			lock.unlock();
		}
	}

	/**
	 * 锁定，输出日志
	 * @param logs 日志集合
	 */
	private void pushLogs(List<LogItem> logs) {
		lock.lock();
		try {
			if (transmitter != null) {
				transmitter.pushLogs(logs);
			}
		} catch (Throwable e) {

		} finally {
			lock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.DisplayLogTrustor#push()
	 */
	@Override
	protected boolean push() {
		DesktopWindow window = DesktopLauncher.getInstance().getWindow();
		if (!window.isVisible()) {
			return false;
		}

		// 输出日志
		List<LogItem> logs = flush();
		if (logs == null || logs.isEmpty()) {
			return false;
		}

		// 显示日志
		pushLogs(logs);
		return true;
	}
}