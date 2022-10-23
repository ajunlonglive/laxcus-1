/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

/**
 * 回显计时器。<br>
 * 定时检查调用器管理池超时，这是一个循环的过程。
 * 
 * @author scott.liang
 * @version 1.0 11/12/2012
 * @since laxcus 1.0
 */
public final class EchoTimer {

	/** 超时间隔时间 **/
	private long interval;

	/** 超时抵达时间 **/
	private long arriveTime;

	/**
	 * 调用回显计时器，指定超时间隔时间。
	 * @param interval 超时间隔时间，单位：毫秒
	 */
	public EchoTimer(long interval) {
		super();
		setInterval(interval);
		arriveTime = System.currentTimeMillis() + interval;
	}

	/**
	 * 设置超时间隔时间。
	 * @param ms 超时间隔时间，单位：毫秒
	 */
	public void setInterval(long ms) {
		interval = ms;
	}

	/**
	 * 返回超时间隔时间。
	 * @return 超时间隔时间，单位：毫秒
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * 判断超时
	 * @return 返回真或者假
	 */
	public boolean isTimeout() {
		return System.currentTimeMillis() >= arriveTime;
	}

	/**
	 * 更新到下一次超时时间，单位：毫秒
	 * @return 下次触发时间
	 */
	public long refresh() {
		return arriveTime += interval;
	}

}