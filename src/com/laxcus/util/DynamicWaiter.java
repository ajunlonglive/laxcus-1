/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

/**
 * 动态等待器。<br>
 * 使用“await”方法进入等待状态，使用“done”方法唤醒它。
 * 
 * @author scott.liang
 * @version 1.0 8/9/2018
 * @since laxcus 1.0
 */
public class DynamicWaiter {

	/** 等待状态，默认是“真” **/
	private boolean awaiting;
	
	/**
	 * 构造默认的动态等待器
	 */
	public DynamicWaiter() {
		super();
		// 进入等待状态
		awaiting = true;
	}

	/**
	 * 唤醒
	 */
	private synchronized void wakeup() {
		try {
			super.notify();
		} catch (IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.error(e);
		}
	}

	/**
	 * 任务延时
	 * @param ms 等待时间，单位：毫秒
	 */
	private synchronized void delay(long ms) {
		try {
			if (ms > 0L) {
				super.wait(ms);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.error(e);
		}
	}
	
	/**
	 * 判断处于等待状态
	 * @return 返回真或者假
	 */
	public boolean isAwaiting() {
		return awaiting;
	}

	/**
	 * 触发唤醒对象
	 */
	public void done() {
		awaiting = false;
		wakeup();
	}

	/**
	 * 进入等待状态，直到返回处理结果
	 */
	public void await() {
		// 进行等待状态
		while (awaiting) {
			delay(500L);
		}
	}

}