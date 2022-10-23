/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.window;

import com.laxcus.watch.*;
import com.laxcus.watch.pool.*;

/**
 * WATCH节点重新登录线程
 * 
 * @author scott.liang
 * @version 1.0 11/7/2019
 * @since laxcus 1.0
 */
class WatchReloginThread implements Runnable {

	/** WATCH窗口界面 **/
	private WatchWindow window;

	/** 线程句柄 **/
	private Thread thread;

	/**
	 * 构造WATCH节点重新登录线程
	 * @param e 启动线程
	 * @param remote 新管理节点地址
	 */
	public WatchReloginThread(WatchWindow e) {
		window = e;
	}

	/**
	 * 启动线程
	 */
	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// 清除用户
		RegisterMemberBasket.getInstance().clear();
		FrontMemberBasket.getInstance().clear();
		
		// 清除节点运行时
		SiteRuntimeBasket.getInstance().clear();
		// 清除节点
		SiteOnWatchPool.getInstance().clear();
		// 释放连接和网络资源
		WatchLauncher.getInstance().logoutWhenWindow();
		// 执行重新登录线程
		window.doReloginThread();
		// 释放线程
		thread = null;
	}

}