/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.terminal;

import com.laxcus.thread.*;
import com.laxcus.util.*;

/**
 * 启动登录线程
 * 
 * @author scott.liang
 * @version 1.0 6/10/2020
 * @since laxcus 1.0
 */
class LaunchThread extends VirtualThread {

	private DynamicWaiter waiter = new DynamicWaiter();

	private boolean success = false;
	
	private TerminalWindow window;
	
	/**
	 * 构造初始化登录线程
	 * @param e 窗口实例
	 */
	public LaunchThread(TerminalWindow e) {
		super();
		window = e;
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
		// 注册
		success = window.__login();
		// 通知完成
		waiter.done();
		// 结束退出
		setInterrupted(true);
	}

	/* (non-Javadoc)
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
