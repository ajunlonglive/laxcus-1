/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.console;

/**
 * 异步读取截获器。<br>
 * 使用“read”方法进入等待状态，使用“done”方法唤醒它。
 * 
 * @author scott.liang
 * @version 1.0 8/9/2018
 * @since laxcus 1.0
 */
public final class ReadAttacher {

	/** 进入绑定状态 **/
	private volatile boolean attached;

	/** 读取结果 **/
	private String result;

	/**
	 * 构造默认的异步读取截获器
	 */
	public ReadAttacher() {
		super();
		// 默认是假
		attached = false;
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
	 * 判断进入绑定状态
	 * @return 返回真或者假
	 */
	public boolean isAttached() {
		return attached;
	}

	/**
	 * 进入“读”状态
	 * @return 返回截获的字符串
	 */
	public String read() {
		attached = true;
		// 进行等待状态
		while (attached) {
			delay(500L);
		}
		return result;
	}

	/**
	 * 线程调用，设置读取结果
	 * @param e 字符串
	 */
	public void done(String e) {
		result = e;
		attached = false;
		// 唤醒线程
		wakeup();
	}

}