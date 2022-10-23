/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch;

import com.laxcus.site.*;
import com.laxcus.util.sound.*;
import com.laxcus.watch.pool.*;

/**
 * 切换管理节点线程
 * 
 * @author scott.liang
 * @version 1.0 11/6/2019
 * @since laxcus 1.0
 */
class SwitchHubThread implements Runnable {

	/** WATCH启动器 **/
	private WatchLauncher launcher;

	/** 新的管理节点 **/
	private Node hub;

	/** 线程句柄 **/
	private Thread thread;

	/**
	 * 构造切换管理节点线程
	 * @param e 启动线程
	 * @param remote 新管理节点地址
	 */
	public SwitchHubThread(WatchLauncher e, Node remote) {
		launcher = e;
		hub = remote;
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
		
		// 清除节点运行时数据
		SiteRuntimeBasket.getInstance().clear();
		// 清除全部节点
		SiteOnWatchPool.getInstance().clear();
		// 释放资源
		launcher.logoutWhenSwitchHub();
		// 播放错误声音
		launcher.playSound(SoundTag.ERROR);
		// 显示界面
		launcher.getWindow().switchHubTo(hub);
		// 释放线程
		thread = null;
	}

}