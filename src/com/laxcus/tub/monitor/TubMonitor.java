/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.monitor;

import com.laxcus.thread.*;
import com.laxcus.util.*;
import com.laxcus.util.net.*;

/**
 * 边缘容器监听服务器类。定义监听端口和安全通信管理配置两个参数，其余由子类实现。<br><br>
 *
 * <b>注意：<br>
 * 节点以网关身份运行时，是绑定通配符地址（IP4: 0.0.0.0），但是仍然要记录实际的IP地址。<br>
 * 同时，为避免通信双方误判，一台物理主机上，只能有一个内网/公网地址。</b><br>
 * 
 * @author scott.liang
 * @version 1.0 10/10/2020
 * @since laxcus 1.0
 */
public abstract class TubMonitor extends MutexThread {

	/** 边缘服务器默认监听端口(TCP/UDP) */
	public final static int LISTEN_PORT = 7766;

	/** 边缘服务器监听地址。网关是通配符地址，非网关是实际IP地址。**/
	private SocketHost defineHost;

	/** SOCKET接收缓冲区，TCP/UDP通用，默认2M */
	private int receiveBufferSize;

	/**
	 * 构造默认的FIXP监听器
	 */
	protected TubMonitor() {
		super();
		// 为了保证有足够的缓存，服务器的接收缓存最小2M
		setReceiveBufferSize(0x200000);
	}

	/**
	 * 返回本地地址，判断顺序：<br>
	 * <br>
	 * 1. 返回用户定义地址。<br>
	 * 2. 上述不成立，返回socket绑定的真实地址。<br>
	 * <br>
	 * 
	 * @return SocketHost实例
	 */
	public SocketHost getLocal() {
		return (defineHost != null ? defineHost : getBindHost());
	}

	/**
	 * 设置外部定义的本地监听地址。<br>
	 * 这个地址区别于socket绑定的地址，是一个用户指定地址。
	 * 例如socket绑定通配符IP后，本处应该是除通配符以后的IP。<br><br>
	 * 
	 * @param e SocketHost实例
	 */
	public void setDefineHost(SocketHost e) {
		// 不允许空指针
		Laxkit.nullabled(e);
		// 真实地址
		defineHost = e;
	}

	/**
	 * 返回外部定义的本地监听地址
	 * @return SocketHost实例，或者空指针
	 */
	public SocketHost getDefineHost() {
		return defineHost;
	}

	/**
	 * 设置接收缓冲区尺寸，最小1M。
	 * @param size 缓冲区尺寸
	 * @return 返回新设置的缓存尺寸
	 */
	public int setReceiveBufferSize(int size) {
		if (size >= 0x100000) {
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
	 * 返回套接字绑定的主机地址，这个地址是真实的主机地址，包括通配符、自回路、内网IP、公网IP
	 * @return 套接字主机地址
	 */
	public abstract SocketHost getBindHost();
	
	/**
	 * 返回套接字绑定的本地端口
	 * @return 已经绑定返回实际端口，否则是0.
	 */
	public abstract int getBindPort();

	/**
	 * 判断套接字处于绑定状态
	 * @return 返回真或者假
	 */
	public abstract boolean isBound();
}