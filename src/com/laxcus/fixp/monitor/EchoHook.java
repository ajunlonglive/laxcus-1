/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.monitor;

import com.laxcus.fixp.*;

/**
 * RPC远程访问钩子。<br><br>
 * 
 * 客户端向服务端发送数据包，服务端返回客户端的主机地址，包括IP地址和端口。<br>
 * 在这个过程中，RPC远程访问钩子等待服务端的返回结果。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/25/2019
 * @since laxcus 1.0
 */
public class EchoHook {

	/** 等待状态，默认是“真” **/
	private boolean awaiting;

	/** 从FIXP PACKET MONITOR返回的应答包 **/
	private Packet packet;

	/** 等待超时时间，默认是-1，无限制 **/
	private long timeout;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {
		packet = null;
	}

	/**
	 * 构造默认的RPC远程访问钩子，无限延时
	 */
	public EchoHook() {
		super();
		awaiting = true;
		timeout = -1; // 默认-1
	}

	/**
	 * RPC远程访问钩子
	 * @param timeout 超时时间，以毫秒计
	 */
	public EchoHook(long timeout) {
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
	 * 设置从FIXP PACKET MONITOR返回的应答包
	 * @param e 从FIXP PACKET MONITOR返回的应答包
	 */
	public final void setPacket(Packet e) {
		packet = e;
		done();
	}

	/**
	 * 返回从FIXP PACKET MONITOR返回的应答包
	 * @return 从FIXP PACKET MONITOR返回的应答包
	 */
	public Packet getPacket() {
		return packet;
	}

}