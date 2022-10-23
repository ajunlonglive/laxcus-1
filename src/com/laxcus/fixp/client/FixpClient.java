/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.client;

import com.laxcus.fixp.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * FIXP协议客户端。<br>
 * 定义一批与SOCKET和安全通信相关的参数。网络连接和发送接收数据由子类去实现
 * 
 * @author scott.liang
 * @version 1.2 12/10/2013
 * @since laxcus 1.0
 */
public class FixpClient {

	/** 服务器主机地址 ，分为TCP/UDP两种模式**/
	private SocketHost remote;

	/** 连接超时，单位：毫秒 */
	protected int connectTimeout;

	/** 接收超时，单位：毫秒 */
	protected int receiveTimeout;

	/** SOCKET接收缓冲区，默认1K */
	protected int receiveBufferSize;

	/** SOCKET发送缓冲区，默认1K */
	protected int sendBufferSize;

	/** 发送的数据流量 **/
	private long receiveFlowSize;

	/** 接收的数据流量 **/
	private long sendFlowSize;

	/**
	 * 构造FIXP客户端，并且指定它的连接模式(TCP/UDP)
	 * @param socketFamily SOCKET连接模式
	 */
	protected FixpClient(byte socketFamily) {
		super();
		remote = new SocketHost(socketFamily);
		// 连接超时60秒
		setConnectTimeout(SocketTransfer.getDefaultConnectTimeout());
		// 接收超时无限制，0是无限期持续等待
		setReceiveTimeout(SocketTransfer.getDefaultReceiveTimeout());
		// 取外部接口定义的缓存尺寸
		setReceiveBufferSize(SocketTransfer.getDefaultReceiveBufferSize());
		setSendBufferSize(SocketTransfer.getDefaultSendBufferSize());

		// 接收/发送流统计是0
		receiveFlowSize = sendFlowSize = 0L;
	}

	/**
	 * 保存FIXP客户端连接的服务器地址
	 * @param e 服务器地址
	 */
	public void setRemote(SocketHost e) {
		if (e == null) {
			remote = null;
		} else if (e.compareTo(remote) != 0) {
			remote = e.duplicate(); // 地址不匹配时保存
		}
	}

	/**
	 * 返回服务器连接地址
	 * @return SocketHost实例
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/**
	 * 延时，单位：毫秒
	 * @param ms 延时时间
	 */
	protected synchronized void delay(long ms) {
		try {
			wait(ms);
		} catch (InterruptedException exp) {

		}
	}

	/**
	 * 唤醒
	 */
	protected synchronized void wakeup() {
		try {
			notify();
		} catch (IllegalMonitorStateException exp) {

		}
	}

	/**
	 * 设置以毫秒为单位的连接超时时间。连接时间，最小是10秒。
	 * 
	 * @param ms 以毫秒为单位的连接超时时间
	 * @return 返回以毫秒为单位的连接超时时间
	 */
	public int setConnectTimeout(int ms) {
		if (ms >= 10000) {
			connectTimeout = ms;
		}
		return connectTimeout;
	}

	/**
	 * 返回以毫秒为单位的连接超时时间
	 * 
	 * @return 以毫秒为单位的连接超时
	 */
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * 设置毫秒为单位的接收超时时间。<br>
	 * TCP/UDP接收时间必须大于0
	 * 
	 * @param ms 毫秒
	 * @return 返回新设置的接收超时
	 */
	public int setReceiveTimeout(int ms) {
		if (ms > 0) {
			receiveTimeout = ms;
		}
		return receiveTimeout;
	}

	/**
	 * 返回毫秒为单位的接收超时时间
	 * @return 毫秒为单位的接收超时时间
	 */
	public int getReceiveTimeout() {
		return receiveTimeout;
	}

	/**
	 * 设置接收缓冲区尺寸
	 * @param size 接收缓冲区尺寸
	 */
	public void setReceiveBufferSize(int size) {
		if (size >= 128) {
			receiveBufferSize = size;
		}
	}

	/**
	 * 返回接收缓冲区尺寸
	 * @return 接收缓冲区尺寸
	 */
	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	/**
	 * 设置发送缓区冲尺寸
	 *
	 * @param size 发送缓冲区尺寸
	 */
	public void setSendBufferSize(int size) {
		if (size >= 128) {
			sendBufferSize = size;
		}
	}

	/**
	 * 返回发送缓冲区尺寸
	 * @return 发送缓冲区尺寸
	 */
	public int getSendBufferSize() {
		return sendBufferSize;
	}

	/**
	 * 增加接收的数据流量
	 * @param size 接收的数据流量尺寸
	 */
	public void addReceiveFlowSize(long size) {
		if (size < 0) {
			throw new IllegalValueException("illegal flow value:%d", size);
		}
		receiveFlowSize += size;
	}

	/**
	 * 返回接收的数据流量
	 * @return 接收的数据流量的整形值
	 */
	public long getReceiveFlowSize() {
		return receiveFlowSize;
	}

	/**
	 * 增加发送的数据流量
	 * @param size 发送的数据流量尺寸
	 */
	public void addSendFlowSize(long size) {
		if (size < 0) {
			throw new IllegalValueException("illegal flow value:%d", size);
		}
		sendFlowSize += size;
	}

	/**
	 * 返回发送的数据流量
	 * @return 发送的数据流量的整型值
	 */
	public long getSendFlowSize() {
		return sendFlowSize;
	}

	/**
	 * 释放SOCKET对象
	 */
	public void destroy() {
		remote = null;
	}

	/**
	 * JVM回收释放资源
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() {		
		destroy();
	}

	/**
	 * 随机生成一个密码，默认指定为AES算法
	 * @return Cipher实例
	 */
	protected Cipher createCipher() {
		return Cipher.create(true);
	}


	//	/** SOCKET连接超时，以毫秒为单位，默认60秒 **/
	//	private static int defaultConnectTimeout = 60000;
	//
	//	/** SOCKET接收超时，以毫秒为单位 **/
	//	private static int defaultReceiveTimeout = 120000;
	//
	//	/** SOCKET接收缓存尺寸 **/
	//	private static int defaultReceiveBufferSize = 102400;
	//
	//	/** SOCKET发送缓存尺寸 **/
	//	private static int defaultSendBufferSize = 102400;

	//	/**
	//	 * 设置以毫秒为单位的连接超时
	//	 * @param ms 毫秒
	//	 * @return 返回设置的连接超时
	//	 */
	//	public static int setDefaultConnectTimeout(int ms) {
	//		if (ms > 0) {
	//			FixpClient.defaultConnectTimeout = ms;
	//		}
	//		return FixpClient.defaultConnectTimeout;
	//	}
	//
	//	/**
	//	 * 返回以毫秒为单位的连接超时
	//	 * @return 毫秒为单位的连接超时
	//	 */
	//	public static int getDefaultConnectTimeout() {
	//		return FixpClient.defaultConnectTimeout;
	//	}
	//
	//	/**
	//	 * 设置以毫秒为单位的接收超时
	//	 * @param ms 毫秒
	//	 * @return 返回设置的接收超时
	//	 */
	//	public static int setDefaultReceiveTimeout(int ms) {
	//		if (ms > 0) {
	//			FixpClient.defaultReceiveTimeout = ms;
	//		}
	//		return FixpClient.defaultReceiveTimeout;
	//	}
	//
	//	/**
	//	 * 返回以毫秒为单位的接收超时
	//	 * @return 毫秒为单位的接收超时
	//	 */
	//	public static int getDefaultReceiveTimeout() {
	//		return FixpClient.defaultReceiveTimeout;
	//	}
	//	
	//	/**
	//	 * 设置默认的接收缓存尺寸
	//	 * @param size 毫秒
	//	 * @return 返回设置的接收缓存尺寸
	//	 */
	//	public static int setDefaultReceiveBufferSize(int size) {
	//		if (size > 0) {
	//			FixpClient.defaultReceiveBufferSize = size;
	//		}
	//		return FixpClient.defaultReceiveBufferSize;
	//	}
	//
	//	/**
	//	 * 返回默认的接收缓存尺寸
	//	 * @return 接收缓存尺寸
	//	 */
	//	public static int getDefaultReceiveBufferSize() {
	//		return FixpClient.defaultReceiveBufferSize;
	//	}
	//	
	//	/**
	//	 * 设置默认的发送缓存尺寸
	//	 * @param size 毫秒
	//	 * @return 返回设置的发送缓存尺寸
	//	 */
	//	public static int setDefaultSendBufferSize(int size) {
	//		if (size > 0) {
	//			FixpClient.defaultSendBufferSize = size;
	//		}
	//		return FixpClient.defaultSendBufferSize;
	//	}
	//
	//	/**
	//	 * 返回默认的发送缓存尺寸
	//	 * @return 发送缓存尺寸
	//	 */
	//	public static int getDefaultSendBufferSize() {
	//		return FixpClient.defaultSendBufferSize;
	//	}

}