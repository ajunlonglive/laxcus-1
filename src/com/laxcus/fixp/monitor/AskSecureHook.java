/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

import com.laxcus.fixp.secure.*;

/**
 * 私有的密文类型查询钩子。<br><br>
 * 
 * 客户端的FixpPacketMonitor，查询服务端的FixpPacketMonitor的密文类型，密文是类型是一个整数值<br>
 * 在这个过程中，私有的密文类型查询钩子等待服务端的返回结果。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/5/2019
 * @since laxcus 1.0
 */
public class AskSecureHook {

	/** 等待状态，默认是“真” **/
	private boolean awaiting;

//	/** 密文类型 **/
//	private int family;
	
	/** 客户机密钥 **/
	private PublicSecure secure;

	/** 等待超时时间，默认是-1，无限制 **/
	private long timeout;

	/** 判断是强制中断  **/
	private boolean interrupt;
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {
		
	}

	/**
	 * 构造默认的私有的密文类型查询钩子，无限延时
	 */
	private AskSecureHook() {
		super();
		awaiting = true;
		timeout = -1; // 默认-1
//		family = SecureType.INVALID;
		secure = null;
		// 强制中断FALSE
		interrupt = false;
	}

	/**
	 * 私有的密文类型查询钩子
	 * @param timeout 超时时间，以毫秒计
	 */
	public AskSecureHook(long timeout) {
		this();
		setTimeout(timeout);
	}
	
	/**
	 * 最大超时时间，对应命令中的Command.timeout
	 * @param ms 毫秒
	 */
	public void setTimeout(long ms) {
		timeout = ms;
	}

	/**
	 * 返回超时时间
	 * @return 时间（长整型）
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 执行延时
	 * @param timeout 超时时间，单位：毫秒
	 */
	private synchronized void delay(long timeout) {
		try {
			super.wait(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 唤醒延时
	 */
	private synchronized void wakeup() {
		try {
			notify();
		} catch (IllegalMonitorStateException e) {
			e.printStackTrace();
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
		// 设置超时触发时间
		long touchTime = -1;
		if (timeout > 0) {
			touchTime = System.currentTimeMillis() + timeout;
		}

		// 进行等待状态
		while (awaiting) {
			// 判断触发超时
			if (touchTime > 0 && System.currentTimeMillis() >= touchTime) {
				done();
				break;
			}
			delay(1000L);
		}
	}
	
	/**
	 * 设置从FixpPacketHelper返回的客户机密钥
	 * @param e 从FixpPacketHelper返回的客户机密钥
	 */
	public void setSecure(PublicSecure e) {
		secure = e;
		done();
	}
	
	/**
	 * 返回FixpPacketHelper返回的客户机密钥
	 * @return PublicSecure
	 */
	public PublicSecure getSecure() {
		return secure;
	}

//	/**
//	 * 设置从FIXP PACKET MONITOR返回的服务端密文类型
//	 * @param e 从FIXP PACKET MONITOR返回的服务端密文类型
//	 */
//	public final void setFamily(int who) {
//		family = who;
//		done();
//	}
//
//	/**
//	 * 返回从FIXP PACKET MONITOR返回的服务端密文类型
//	 * @return 从FIXP PACKET MONITOR返回的服务端密文类型
//	 */
//	public int getFamily() {
//		return family;
//	}

	/**
	 * 强制中止
	 */
	public void interrupt() {
		interrupt = true;
		done();
	}

	/**
	 * 判断强制中止
	 * @return 返回真或者假
	 */
	public boolean isInterrupted() {
		return interrupt;
	}
}