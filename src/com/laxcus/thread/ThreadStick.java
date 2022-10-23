/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.thread;

/**
 * 线程感应器
 * 
 * @author scott.liang
 * @version 1.0 3/12/2009
 * @since laxcus 1.0
 */
public class ThreadStick {

	/** 当置“真”，表示收到线程通知 **/
	private boolean okay;

	/**
	 * 构造默认的线程感应器
	 */
	public ThreadStick() {
		super();
		okay = false;
	}

	/**
	 * 判断完成
	 * @return 返回真或者假
	 */
	public boolean isOkay() {
		return okay;
	}

	/**
	 * 延时
	 * @param timeout
	 */
	public synchronized void delay(long timeout) {
		try {
			wait(timeout);
		}catch(InterruptedException e) {

		}
	}

	/**
	 * 通知完成
	 */
	public synchronized void wakeup() {
		okay = true;
		try {
			notify();
		}catch(IllegalMonitorStateException e) {

		}
	}

}