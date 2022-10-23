/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.window;

import com.laxcus.thread.*;
import com.laxcus.util.*;

/**
 * 初始化登录启动线程
 * 
 * @author scott.liang
 * @version 1.0 6/10/2020
 * @since laxcus 1.0
 */
class LaunchThread extends VirtualThread {

	private DynamicWaiter waiter = new DynamicWaiter();

	private boolean success = false;

	private WatchWindow window;

	/**
	 * 构造初始化登录启动线程
	 */
	public LaunchThread(WatchWindow e) {
		super();
		window = e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		// 注册
		success = window.__login();
		// 结束退出
		setInterrupted(true);
		// 通知完成
		waiter.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.laxcus.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() { }

	/**
	 * 等待
	 */
	public void await() {
		waiter.await();
	}

	public boolean isSuccessful() {
		return success;
	}

}