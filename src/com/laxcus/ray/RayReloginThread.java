/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray;

import com.laxcus.log.client.*;
import com.laxcus.platform.*;
import com.laxcus.platform.listener.*;
import com.laxcus.ray.runtime.*;

/**
 * RAY.WINDOW节点重新登录线程
 * 
 * @author scott.liang
 * @version 1.0 11/7/2019
 * @since laxcus 1.0
 */
class RayReloginThread implements Runnable {

	/** RAY.WINDOW窗口界面 **/
	private RayWindow window;

	/** 线程句柄 **/
	private Thread thread;

	/**
	 * 构造RAY.WINDOW节点重新登录线程
	 * @param e 启动线程
	 * @param remote 新管理节点地址
	 */
	public RayReloginThread(RayWindow e) {
		window = e;
	}

	/**
	 * 启动线程
	 */
	public void start() {
		thread = new Thread(this);
		thread.start();
	}
	
	/**
	 * 清除当前客户端的记录
	 */
	private void clearClient() {
		WatchClient[] as = PlatformKit.findListeners(WatchClient.class);
		int size = (as != null ? as.length : 0);
		// 清除客户端UI上的显示和内存记录
		for (int i = 0; i < size; i++) {
			try {
				as[i].release();
			} catch (Throwable e) {
				Logger.fatal(e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// 清除用户
		RayRegisterMemberBasket.getInstance().clear();
		RayFrontMemberBasket.getInstance().clear();
		
		// 清除节点运行时
		RaySiteRuntimeBasket.getInstance().clear();
		// 清除节点
		SiteOnRayPool.getInstance().clear();
		
		// 清除客户端
		clearClient();
		
		// 释放连接和网络资源
		RayLauncher.getInstance().logoutWhenWindow();
		// 执行重新登录线程
		window.doReloginThread();
		// 释放线程
		thread = null;
	}
	

//	/* (non-Javadoc)
//	 * @see java.lang.Runnable#run()
//	 */
//	@Override
//	public void run() {
//		// 释放连接和网络资源
//		RayLauncher.getInstance().disconnect();
//		// 执行重新登录线程
//		window.doReloginThread();
//		// 释放线程
//		thread = null;
//	}

}