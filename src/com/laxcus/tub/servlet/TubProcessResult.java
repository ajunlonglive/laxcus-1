/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet;

import java.io.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.net.*;

/**
 * 边缘容器处理结果
 * 
 * @author scott.liang
 * @version 1.0 6/20/2019
 * @since laxcus 1.0
 */
public abstract class TubProcessResult implements Classable, Serializable, Cloneable {
	
	private static final long serialVersionUID = 5632325959196993894L;

	/** 执行结果 **/
	private final static int NONE = 0;
	
	public static final int NOTFOUND = -2;

	public static final int FAILED = -1;

	public static final int SUCCESSFUL = 1;
	
	/** 状态码  **/
	private int status = TubProcessResult.NONE;
	
	/** 命名 **/
	private Naming naming;
	
	/** 本地主机 **/
	private SocketHost host;

	/** 进程ID **/
	private long processId;

	/** 线程ID **/
	private long threadId;

	/**
	 * 构造默认的边缘容器处理结果
	 */
	protected TubProcessResult() {
		super();
		status = TubProcessResult.NONE;
		processId = TubIdentity.INVALID;
		threadId = -1;
	}

	/**
	 * 设置边缘容器处理结果
	 * @param that 边缘容器处理结果
	 */
	protected TubProcessResult(TubProcessResult that) {
		this();
		status = that.status;
		processId = that.processId;
		threadId = that.threadId;
		if (that.naming != null) {
			naming = that.naming.duplicate();
		}
		if (that.host != null) {
			host = that.host.duplicate();
		}
	}

	/**
	 * 设置结果状态
	 * @param who 状态码
	 */
	public void setStatus(int who) {
		status = who;
	}

	/**
	 * 返回结果状态
	 * @return 状态码
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * 判断是成功
	 * @return 真或者假
	 */
	public boolean isSuccessful() {
		return status > TubProcessResult.NONE;
	}

	/**
	 * 判断是失败
	 * @return 真或者假
	 */
	public boolean isFailed() {
		return status < TubProcessResult.NONE;
	}

	/**
	 * 设置容器命名
	 * @param e Naming实例
	 */
	public void setNaming(Naming e) {
		naming = e;
	}

	/**
	 * 返回容器命名
	 * @return Naming实例
	 */
	public Naming getNaming() {
		return naming;
	}

	/**
	 * 设置通信主机
	 * @param e
	 */
	public void setHost(SocketHost e) {
		host = e;
	}

	/**
	 * 返回通信主机
	 * @return
	 */
	public SocketHost getHost() {
		return host;
	}

	/**
	 * 设置容器进程ID，大于或者等于0
	 * @param e Naming实例
	 */
	public void setProcessId(long e) {
		processId = e;
	}

	/**
	 * 返回容器进程ID，大于或者等于0
	 * @return Naming实例
	 */
	public long getProcessId() {
		return processId;
	}

	/**
	 * 设置线程编号，来自系统线程
	 * @param who 线程编号
	 */
	public void setThreadId(long who) {
		threadId = who;
	}

	/**
	 * 返回线程编号
	 * @return 线程编号
	 */
	public long getThreadId() {
		return threadId;
	}

	/**
	 * 将命令参数写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeInt(status);
		writer.writeInstance(naming);
		writer.writeInstance(host);
		writer.writeLong(processId);
		writer.writeLong(threadId);
		// 调用子类接口，将子类信息写入可类化存储器
		buildSuffix(writer);
		// 返回写入的数据长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析命令参数。
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		status = reader.readInt();
		naming = reader.readInstance(Naming.class);
		host = reader.readInstance(SocketHost.class);
		processId = reader.readLong();
		threadId = reader.readLong();
		// 从可类化读取器中解析子类信息
		resolveSuffix(reader);
		// 返回读取的数据长度
		return reader.getSeek() - seek;
	}

	/**
	 * 子类对象生成自己的浅层数据副本。<br>
	 * @return TubProcessResult子类实例
	 */
	public abstract TubProcessResult duplicate();

	/**
	 * 将操作命令参数信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析操作命令参数信息
	 * @param reader 可类化数据读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader);
}
