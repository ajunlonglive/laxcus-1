/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.pool.schedule;

import com.laxcus.util.*;

/**
 * 默认的串行处理工作
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus 1.0
 */
public class DefaultSerialSchedule implements SerialSchedule {
	
	/** 资源名称 **/
	private String resource;

	/** 获得资源锁定和使用权利 **/
	private boolean attached;

	/**
	 * 构造默认的串行处理工作，指定被处理的资源
	 * @param resource - 资源名称
	 */
	public DefaultSerialSchedule(String resource) {
		super();
		attached = false;
		setResource(resource);
	}
	
	/**
	 * 唤醒
	 */
	protected synchronized void wakeup() {
		try {
			this.notify();
		}catch(IllegalMonitorStateException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}

	/**
	 * 延时
	 * @param timeout
	 */
	protected synchronized void delay(long timeout) {
		try {
			if (timeout > 0L) {
				super.wait(timeout);
			}
		} catch (InterruptedException e) {
			com.laxcus.log.client.Logger.fatal(e);
		}
	}
	
	/**
	 * 设置资源名称
	 * @param e
	 */
	public void setResource(String e) {
		Laxkit.nullabled(e);

		resource = e;
	}
	
	/**
	 * 返回资源名称
	 * @return String
	 */
	public String getResource() {
		return resource;
	}
	
	/**
	 * 执行锁定，直到获得操作这个资源的权利
	 */
	public void lock() {
		// 申请串处理工作的权利（每次只有一个线程获得）
		attached = SerialSchedulePool.getInstance().admit(resource, this);
		// 如果没有获得资源，一直等待，直到完成
		while (!attached) {
			this.delay(1000);
		}
	}
	
	/**
	 * 解除对一个表的锁定。
	 * @return 返回真或者假
	 */
	public boolean unlock() {
		return SerialSchedulePool.getInstance().release(resource, this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.schedule.SerialSchedule#attach()
	 */
	@Override
	public void attach() {
		attached = true;
		wakeup();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.schedule.SerialSchedule#isAttached()
	 */
	@Override
	public boolean isAttached() {
		return attached;
	}

}
