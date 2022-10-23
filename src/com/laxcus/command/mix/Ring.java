/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import com.laxcus.command.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.net.*;

/**
 * 类似LINUX PING命令的通信检测命令。<br>
 * 检测当前节点与目标节点之间的socket通信，以及加密参数的正确性。<br>
 * 
 * @author scott.liang
 * @version 1.0 2/1/2019
 * @since laxcus 1.0
 */
public class Ring extends Command {

	private static final long serialVersionUID = -3594231475829705362L;

	/** 目标站点 **/
	private SocketHost remote;
	
	/** socket明文 **/
	private String socket;
	
	/** 加密模式，默认是真 **/
	private boolean secure;
	
	/** 测试统计 **/
	private int count;
	
	/** 接收超时，以毫秒为单位 **/
	private int socketTimeout;
	
	/** 连续发送时的延时间隔时间，以毫秒为单位，默认是无间隔 **/
	private int delay;

	/**
	 * 构造默认的命令实例
	 */
	public Ring() {
		super();
		secure = true;
		count = 1;
		socketTimeout = 20000; // 20毫秒
		delay = 0;	// 默认无间隔时间
	}

	/**
	 * 构造命令实例，指定参数
	 * @param remote 目标FIXP服务器
	 */
	public Ring(SocketHost remote) {
		this();
		setRemote(remote);
	}
	
	/**
	 * 从可类化读取器中解析命令实例
	 * @param reader 可类化数据读取器
	 */
	public Ring(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 生成数据副本
	 * @param that 传入实例
	 */
	private Ring(Ring that) {
		super(that);
		remote = that.remote;
		secure = that.secure;
		count = that.count;
		socket = that.socket;
		socketTimeout = that.socketTimeout;
		delay = that.delay;
	}

	/**
	 * 设置加密通信模式
	 * @param b 真或者假
	 */
	public void setSecure(boolean b) {
		secure = b;
	}

	/**
	 * 判断加密通信模式
	 * @return 真或者假
	 */
	public boolean isSecure() {
		return secure;
	}

	/**
	 * 设置以毫秒为单位的延时时间
	 * @param ms 延时时间
	 */
	public void setSocketTimeout(int ms) {
		if (ms > 0) socketTimeout = ms;
	}

	/**
	 * 返回以毫秒为单位的延时时间
	 * @return 延时时间
	 */
	public int getSocketTimeout() {
		return socketTimeout;
	}

	/**
	 * 设置延时间隔时间
	 * @param ms 延时间隔时间
	 */
	public void setDelay(int ms) {
		if (ms >= 0) delay = ms;
	}

	/**
	 * 返回延时间隔时间
	 * @return 延时间隔时间
	 */
	public int getDelay() {
		return delay;
	}
	
	/**
	 * 测试统计
	 * @param i 测试统计值
	 */
	public void setCount(int i) {
		if (i > 0) count = i;
	}

	/**
	 * 返回测试统计
	 * @return 测试统计
	 */
	public int getCount() {
		return count;
	}

	/**
	 * 设置目标FIXP服务器明文地址
	 * @param e
	 */
	public void setSocket(String e) {
		Laxkit.nullabled(e);
		socket = e;
	}

	/**
	 * 返回目标FIXP服务器明文地址
	 * @return FIXP服务器地址
	 */
	public String getSocket() {
		return socket;
	}

	/**
	 * 设置目标FIXP服务器地址
	 * @param e
	 */
	public void setRemote(SocketHost e) {
		Laxkit.nullabled(e);
		remote = e;
	}

	/**
	 * 返回目标FIXP服务器地址
	 * @return FIXP服务器地址
	 */
	public SocketHost getRemote() {
		return remote;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#duplicate()
	 */
	@Override
	public Ring duplicate() {
		return new Ring(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(remote);
		writer.writeBoolean(secure);
		writer.writeInt(count);
		writer.writeString(socket);
		writer.writeInt(socketTimeout);
		writer.writeInt(delay);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.command.Command#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		remote = new SocketHost(reader);
		secure = reader.readBoolean();
		count = reader.readInt();
		socket = reader.readString();
		socketTimeout = reader.readInt();
		delay = reader.readInt();
	}

}