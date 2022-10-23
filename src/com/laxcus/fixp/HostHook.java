/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import com.laxcus.util.net.*;

/**
 * 网络主机检测钩子。<br><br>
 * 
 * 客户端向服务端发送数据包，服务端返回客户端的主机地址，包括IP地址和端口。<br>
 * 在这个过程中，网络主机检测钩子等待服务端的返回结果。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/25/2019
 * @since laxcus 1.0
 */
public class HostHook {

	/** 等待状态，默认是“真” **/
	private boolean awaiting;

	/** 从服务器端检测返回的本地地址 **/
	private SocketHost local;

	/** 等待超时时间，默认是-1，无限制 **/
	private long timeout;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {
		local = null;
	}

	/**
	 * 构造默认的网络主机检测钩子，无限延时
	 */
	public HostHook() {
		super();
		awaiting = true;
		timeout = -1; // 默认-1
	}

	/**
	 * 网络主机检测钩子
	 * @param timeout 超时时间，以毫秒计
	 */
	public HostHook(long timeout) {
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

//	/**
//	 * 设置执行过程中产生的故障对象，同时唤醒等待
//	 * @param e 故障对象
//	 */
//	public void setFault(Throwable e) {
//		fault = e;
//		done();
//	}
//
//	/**
//	 * 返回执行过程中产生的故障对象
//	 * @return 故障对象
//	 */
//	public Throwable getFault() {
//		return fault;
//	}
//
//	/**
//	 * 判断有故障
//	 * @return 返回真或者假
//	 */
//	public boolean isFault() {
//		return fault != null;
//	}

	/**
	 * 设置从服务器端检测返回的本地地址
	 * @param e 本地地址
	 */
	public final void setLocal(SocketHost e) {
		local = e;
		done();
	}

	/**
	 * 返回从服务器端检测返回的本地地址
	 * @return 本地地址
	 */
	public SocketHost getLocal() {
		return local;
	}

}