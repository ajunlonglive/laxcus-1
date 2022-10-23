/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

import com.laxcus.util.net.*;

/**
 * 删除密文钩子。<br><br>
 * 
 * 客户端的FixpPacketMonitor，删除服务端的FixpPacketMonitor的密文<br>
 * 在这个过程中，删除密文钩子等待服务端的返回结果。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/6/2019
 * @since laxcus 1.0
 */
public class DropSecureHook {
	
	/** 目标地址 **/
	private SocketHost remote;
	
	/** 序列号 **/
	private long serial;

	/** 等待状态，默认是“真” **/
	private boolean awaiting;
	
	/** 判断唤醒是来自外部的触发 **/
	private boolean touched;

	/** 删除成功或者否 **/
	private boolean successful;

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
		remote = null;
	}

	/**
	 * 构造默认的删除密文钩子，无限延时
	 */
	private DropSecureHook() {
		super();
		awaiting = true;
		timeout = -1; // 默认-1
		// 触发否
		touched = false;
		// 成功否
		successful = false;
		// 强制中断
		interrupt = false;
	}

	/**
	 * 删除密文钩子
	 * @param remote SocketHost实例
	 * @param serial 序列号
	 * @param timeout 超时时间，以毫秒计
	 */
	public DropSecureHook(SocketHost remote, long serial, long timeout) {
		this();
		setRemote(remote);
		setSerial(serial);
		setTimeout(timeout);
	}

	/**
	 * 设置目标地址
	 * @param e SocketHost实例
	 */
	public void setRemote(SocketHost e) {
		remote = e;
	}

	/**
	 * 返回目标地址
	 * @return SocketHost实例
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/**
	 * 设置序列号
	 * @param who
	 */
	public void setSerial(long who) {
		serial = who;
	}

	/**
	 * 返回序列号
	 * @return
	 */
	public long getSerial() {
		return serial;
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
	 * 判断是来自外部的触发
	 * @return 返回真或者假
	 */
	public boolean isTouched() {
		return touched;
	}

	/**
	 * 设置从FIXP PACKET MONITOR返回的服务端删除成功或者否
	 * @param e 从FIXP PACKET MONITOR返回的服务端删除成功或者否
	 */
	public final void setSuccessful(boolean who) {
		successful = who;
		touched = true;
		done();
	}

	/**
	 * 返回从FIXP PACKET MONITOR返回的服务端删除成功或者否
	 * @return 从FIXP PACKET MONITOR返回的服务端删除成功或者否
	 */
	public boolean isSuccessful() {
		return successful;
	}

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