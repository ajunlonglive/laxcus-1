/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.console;

import com.laxcus.front.*;
import com.laxcus.util.net.*;

/**
 * 控制台登录追踪器
 * 
 * @author scott.liang
 * @version 1.0 8/29/2018
 * @since laxcus 1.0
 */
public class ConsoleTracker implements FrontLoginTracker, Runnable {

	/** 线程句柄  **/
	private Thread thread;

	/** 停止 **/
	private volatile boolean stopped;

	/** 运行标记 **/
	private volatile boolean running;

	/**
	 * 构造默认的控制台登录追踪器
	 */
	public ConsoleTracker() {
		super();
		stopped = false;
		running = false;
	}

	/**
	 * 线程延时等待。单位：毫秒。
	 * @param timeout 超时时间
	 */
	public synchronized void delay(long timeout) {
		try {
			if (timeout > 0L) {
				wait(timeout);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 唤醒线程
	 */
	public synchronized void wakeup() {
		try {
			notify();
		}catch(IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.LoginTracker#start()
	 */
	@Override
	public void start() {
		thread = new Thread(this);
		thread.start();

		// 直到启动!
		while (!isRunning()) {
			delay(100);
		}
	}

	/**
	 * 判断处于运行状态
	 * @return 返回真或者假
	 */
	public boolean isRunning() {
		return running && thread != null;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.front.LoginTracker#stop()
	 */
	@Override
	public void stop() {
		stopped = true;
		wakeup();
		// 在运行状态，等待！
		while (isRunning()) {
			delay(100);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		running = true;
		// 在控制台窗口打印字符
		while (!stopped) {
			System.out.print(".");
			delay(1000);
		}
		// 控制台换行！
		System.out.println();
		// 清除参数
		running = false;
		thread = null;
	}

	/** 获取码 **/
	private int pitchId;
	
	/** 登录地址 **/
	private SiteHost pitchHub;
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLoginTracker#setPitchId(int)
	 */
	@Override
	public void setPitchId(int who) {
		pitchId = who;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLoginTracker#getPitchId()
	 */
	@Override
	public int getPitchId() {
		return pitchId;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLoginTracker#setPitchHub(com.laxcus.util.net.SiteHost)
	 */
	@Override
	public void setPitchHub(SiteHost host) {
		pitchHub = host;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.FrontLoginTracker#getPitchHub()
	 */
	@Override
	public SiteHost getPitchHub() {
		return pitchHub;
	}	
}
