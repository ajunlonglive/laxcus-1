/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front;

/**
 * 重启GATE站点注册
 * 
 * @author scott.liang
 * @version 1.0 5/28/2019
 * @since laxcus 1.0
 */
class GateForsakeThread implements Runnable {

	/** 前端启动器 **/
	private FrontLauncher launcher;

	/** 线程句柄 **/
	private Thread thread;

	/**
	 * 构造默认的重启GATE站点注册
	 */
	public GateForsakeThread(FrontLauncher e) {
		super();
		launcher = e;
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
		// 显示登录中断！
		launcher.forsake();
		thread = null;
	}

}