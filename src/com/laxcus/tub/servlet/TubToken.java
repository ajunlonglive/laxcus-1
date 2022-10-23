/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.net.*;

/**
 * 边缘容器符号 <br>
 * 
 * 由边缘容器命名和其它参数组成，描述一个运行中的边缘容器的基础属性。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2019
 * @since laxcus 1.0
 */
public final class TubToken implements Serializable, Cloneable, Classable, Markable, Comparable<TubToken> {

	private static final long serialVersionUID = 3501607725390885344L;

	/** 边缘容器名称 */
	private Naming naming;
	
	/** 绑定主机地址 **/
	private SocketHost host;

	/** 进程ID，大于或者等于0 **/
	private long processId;

	/** 线程编号，初始-1，无效 **/
	private long threadId;

	/** 启动时间。在构造时生成，不能修改 **/
	private long launchTime;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(naming);
		writer.writeInstance(host);
		writer.writeLong(processId);
		writer.writeLong(threadId);
		writer.writeLong(launchTime);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		naming = new Naming(reader);
		host = reader.readInstance(SocketHost.class);
		processId = reader.readLong();
		threadId = reader.readLong();
		launchTime = reader.readLong();
		return reader.getSeek() - seek;
	}
	
	/**
	 * 根据传入对象生成它的数据副本
	 * @param that TubTubItem实例
	 */
	private TubToken(TubToken that) {
		this();
		naming = that.naming.duplicate();
		if(that.host!=null){
			host = that.host.duplicate();
		}
		processId = that.processId;
		threadId = that.threadId;
		launchTime = that.launchTime;
	}

	/**
	 * 构造默认的边缘容器符号
	 */
	protected TubToken() {
		super();
		processId = TubIdentity.INVALID;
		threadId = -1;
		// 启动时间
		launchTime = System.currentTimeMillis();
	}

	/**
	 * 构造边缘容器符号，指定边缘容器和类名
	 * @param naming 命名
	 * @param processId 进程编号
	 */
	public TubToken(Naming naming, long processId) {
		this();
		setNaming(naming);
		setId(processId);
	}

	/**
	 * 从可类化数据读取器中解析边缘容器符号参数
	 * @param reader 可类化数据读取器
	 */
	public TubToken(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 从标记化读取器中取出边缘容器符号
	 * @param reader 标记化读取器
	 */
	public TubToken(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置主机地址，允许空指针
	 * @param e
	 */
	public void setHost(SocketHost e) {
		host = e;
	}

	/**
	 * 返回主机地址
	 * @return SocketHost实例或者空指针
	 */
	public SocketHost getHost() {
		return host;
	}
	
	/**
	 * 设置任务ID。运行状态下的任务编号。
	 * @param who 编号
	 */
	public void setId(long who) {
		processId = who;
	}

	/**
	 * 设置进程ID。运行状态下的任务编号。
	 * @return 返回编号
	 */
	public long getId() {
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
	 * 设置边缘容器
	 * @param e 命名实例
	 */
	public void setNaming(Naming e) {
		Laxkit.nullabled(e);

		naming = e;
	}

	/**
	 * 返回边缘容器
	 * @return Naming实例
	 */
	public Naming getNaming() {
		return naming;
	}

	/**
	 * 返回开始时间，单位：毫秒
	 * @return 系统时间
	 */
	public long getLaunchTime() {
		return launchTime;
	}

	/**
	 * 返回任务运行时间
	 * @return 调用器运行时间
	 */
	public long getRunTime() {
		return System.currentTimeMillis() - launchTime;
	}

	/**
	 * 返回边缘容器符号的数据副本
	 * @return TubToken实例
	 */
	public TubToken duplicate() {
		return new TubToken(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TubToken.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TubToken) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) ((processId >>> 32) ^ processId);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%d", naming, processId);
	}

	/*
	 * 比较两个边缘容器符号的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TubToken that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		// 名称是唯一判断标准
		return Laxkit.compareTo(processId, that.processId);
	}


}