/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

/**
 * FRONT.TERMINAL节点注销线程
 * 
 * @author scott.liang
 * @version 1.0 5/21/2021
 * @since laxcus 1.0
 */
class DesktopLogoutThread implements Runnable {

	/** FRONT.TERMINAL窗口界面 **/
	private DesktopWindow window;

	/** 线程句柄 **/
	private Thread thread;

	/**
	 * 构造FRONT.TERMINAL节点注销线程
	 * @param e 启动线程
	 * @param remote 新管理节点地址
	 */
	public DesktopLogoutThread(DesktopWindow e) {
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
		// 释放连接和网络资源
		DesktopLauncher.getInstance().disconnect();
		// 执行注销线程
		window.doLogoutThread();
		
		// 释放线程
		thread = null;
	}

}