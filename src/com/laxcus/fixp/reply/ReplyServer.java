/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 异步通信服务器。
 * 是REPLY MONITOR和REPLY DISPATCHER的基础类。
 * 
 * @author scott.liang
 * @version 1.0 7/22/2018
 * @since laxcus 1.0
 */
public abstract class ReplyServer extends MutexThread {

	/** 默认监听端口 **/
	protected int defaultPort;

	/** 映射端口，由管理员设置 **/
	protected int reflectPort;

	/** SOCKET接收缓冲区，TCP/UDP通用，默认2M */
	protected int receiveBufferSize;

	/** UDP SOCKET发送缓冲区，默认1M */
	protected int sendBufferSize;
	
	/** 内部网络地址 **/
	protected Address definePrivateIP;

	/** 外部网络地址 **/
	protected Address definePublicIP;

	/**
	 * 构造默认的异步通信服务器
	 */
	protected ReplyServer() {
		super();
		// 内部网络地址是必须存在的，外部网络地址是一个可选项。
		definePrivateIP = new Address();
		// 默认端口是0
		setDefaultPort(0);
		setReflectPort(0);
	}

	/**
	 * 设置默认监听端口
	 * @param who 监听端口
	 * @return 返回新的默认监听端口
	 */
	public int setDefaultPort(int who) {
		// 检查有效性
		if (who >= 0 || who < 0xFFFF) {
			defaultPort = who;
		}
		return defaultPort;
	}

	/**
	 * 返回默认监听端口
	 * @return 默认监听端口
	 */
	public int getDefaultPort(){
		return defaultPort;
	}

	/**
	 * 设置动态映射端口，由管理员设置
	 * @param who 监听端口
	 */
	public void setReflectPort(int who) {
		if (!SocketTag.isPort(who)) {
			throw new IllegalValueException("illegal port %d", who);
		}
		reflectPort = who;
	}
	
	/**
	 * 返回动态映射端口，由管理员设置
	 * @return 动态映射端口
	 */
	public int getReflectPort(){
		return reflectPort;
	}

	/**
	 * 设置接收缓冲区尺寸，最小1M。
	 * @param size 缓冲区尺寸
	 * @return 返回新设置的缓存尺寸
	 */
	public int setReceiveBufferSize(int size) {
		if (size > 0) {
			receiveBufferSize = size;
		}
		return receiveBufferSize;
	}

	/**
	 * 返回接收缓冲区尺寸
	 * @return 缓冲区尺寸
	 */
	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	/**
	 * 设置发送缓冲区尺寸
	 * @param size 缓冲区尺寸
	 * @return 返回新设置的缓存尺寸
	 */
	public int setSendBufferSize(int size) {
		if (size > 0) {
			sendBufferSize = size;
		}
		return sendBufferSize;
	}

	/**
	 * 返回发送缓冲区尺寸
	 * @return 缓冲区尺寸
	 */
	public int getSendBufferSize() {
		return sendBufferSize;
	}

	/**
	 * 判断绑定网关地址
	 * @return 返回真或者假
	 */
	public boolean isGateway() {
		return definePublicIP != null && definePrivateIP != null;
	}
	
	/**
	 * 设置运行时定义的公网IP地址。
	 * 
	 * @param e Address实例
	 */
	public void setDefinePublicIP(Address e) {
		definePublicIP = e;
	}

	/**
	 * 返回运行时定义的公网IP地址。
	 * 公网IP地址只在splitGatewayConfig函数中定义，此外无它！
	 * 
	 * @return Address实例，没有返回空指针
	 */
	public Address getDefinePublicIP() {
		return definePublicIP;
	}

	/**
	 * 设置运行时定义的内网IP地址，不允许空指针。
	 * 内网IP地址根据运行时的状态进行动态调整。
	 * 
	 * @param e Address实例
	 */
	public void setDefinePrivateIP(Address e) {
		Laxkit.nullabled(e);
		definePrivateIP = e;
	}

	/**
	 * 返回运行时定义的内网IP地址
	 * @return Address实例
	 */
	public Address getDefinePrivateIP() {
		return definePrivateIP;
	}
}
