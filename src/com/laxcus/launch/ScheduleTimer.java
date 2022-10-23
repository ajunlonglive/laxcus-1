/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.launch;

import java.util.*;

/**
 * 任务定时回收器
 * 
 * @author scott.liang
 * @version 1.0 5/31/2019
 * @since laxcus 1.0
 */
public class ScheduleTimer extends Timer {
	
	/** 记录最后的刷新时间 **/
	private long scaleTime;

	/**
	 * 构造默认的任务定时回收器
	 */
	public ScheduleTimer() {
		super();
		refreshTime();
	}

	/**
	 * 构造任务定时回收器，指定为守护模式
	 * @param isDaemon
	 */
	public ScheduleTimer(boolean isDaemon) {
		super(isDaemon);
		refreshTime();
	}

	/**
	 * 构造任务定时回收器，指定名称
	 * @param name
	 */
	public ScheduleTimer(String name) {
		super(name);
		refreshTime();
	}

	/**
	 * @param name
	 * @param isDaemon
	 */
	public ScheduleTimer(String name, boolean isDaemon) {
		super(name, isDaemon);
		refreshTime();
	}

	/**
	 * 刷新时间
	 */
	public void refreshTime() {
		scaleTime = System.currentTimeMillis();
	}

	/**
	 * 判断达到超时时间
	 * @param timeout 以毫秒计的超时时间
	 * @return 返回真或者假
	 */
	public boolean isTimeout(long timeout) {
		return System.currentTimeMillis() - scaleTime >= timeout;
	}

}
