/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.client;

/**
 * 客户端SOCKET公共参数
 * @author scott.liang
 * 
 * @version 1.0 3/3/2019
 * @since laxcus 1.0
 */
public class SocketTransfer {

	/** SOCKET信道UDP包超时，发送信令时使用 **/
	private static int defaultChannelTimeout = 60000;
	
	/** SOCKET连接超时，以毫秒为单位，默认60秒 **/
	private static int defaultConnectTimeout = 60000;

	/** SOCKET接收超时，以毫秒为单位 **/
	private static int defaultReceiveTimeout = 120000;

	/** SOCKET接收缓存尺寸 **/
	private static int defaultReceiveBufferSize = 102400;

	/** SOCKET发送缓存尺寸 **/
	private static int defaultSendBufferSize = 102400;

	/**
	 * 设置UDP通信信令超时时间，不能小于10毫秒。单位：毫秒
	 * @param ms 毫秒
	 */
	public static void setDefaultChannelTimeout(int ms) {
		if (ms >= 10000) {
			SocketTransfer.defaultChannelTimeout = ms;
		}
	}

	/**
	 * 返回UDP通信信令超时时间
	 * @return 返回以毫秒为单位的整数
	 */
	public static int getDefaultChannelTimeout() {
		return SocketTransfer.defaultChannelTimeout;
	}
	
	/**
	 * 设置以毫秒为单位的连接超时
	 * @param ms 毫秒
	 * @return 返回设置的连接超时
	 */
	public static int setDefaultConnectTimeout(int ms) {
		if (ms > 0) {
			SocketTransfer.defaultConnectTimeout = ms;
		}
		return SocketTransfer.defaultConnectTimeout;
	}

	/**
	 * 返回以毫秒为单位的连接超时
	 * @return 毫秒为单位的连接超时
	 */
	public static int getDefaultConnectTimeout() {
		return SocketTransfer.defaultConnectTimeout;
	}

	/**
	 * 设置以毫秒为单位的接收超时
	 * @param ms 毫秒
	 * @return 返回设置的接收超时
	 */
	public static int setDefaultReceiveTimeout(int ms) {
		if (ms > 0) {
			SocketTransfer.defaultReceiveTimeout = ms;
		}
		return SocketTransfer.defaultReceiveTimeout;
	}

	/**
	 * 返回以毫秒为单位的接收超时
	 * @return 毫秒为单位的接收超时
	 */
	public static int getDefaultReceiveTimeout() {
		return SocketTransfer.defaultReceiveTimeout;
	}
	
	/**
	 * 设置默认的接收缓存尺寸
	 * @param size 毫秒
	 * @return 返回设置的接收缓存尺寸
	 */
	public static int setDefaultReceiveBufferSize(int size) {
		if (size > 0) {
			SocketTransfer.defaultReceiveBufferSize = size;
		}
		return SocketTransfer.defaultReceiveBufferSize;
	}

	/**
	 * 返回默认的接收缓存尺寸
	 * @return 接收缓存尺寸
	 */
	public static int getDefaultReceiveBufferSize() {
		return SocketTransfer.defaultReceiveBufferSize;
	}
	
	/**
	 * 设置默认的发送缓存尺寸
	 * @param size 毫秒
	 * @return 返回设置的发送缓存尺寸
	 */
	public static int setDefaultSendBufferSize(int size) {
		if (size > 0) {
			SocketTransfer.defaultSendBufferSize = size;
		}
		return SocketTransfer.defaultSendBufferSize;
	}

	/**
	 * 返回默认的发送缓存尺寸
	 * @return 发送缓存尺寸
	 */
	public static int getDefaultSendBufferSize() {
		return SocketTransfer.defaultSendBufferSize;
	}
}
