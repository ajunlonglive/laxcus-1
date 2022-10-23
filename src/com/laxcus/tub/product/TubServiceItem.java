/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.product;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.net.*;

/**
 * 运行中的边缘容器单元 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/18/2020
 * @since laxcus 1.0
 */
public final class TubServiceItem implements Cloneable, Classable, Comparable<TubServiceItem> {

	/** 边缘容器名称 */
	private Naming naming;

	/** 绑定主机地址 **/
	private SocketHost host;

	/** 进程ID，大于或者等于0 **/
	private long processId;

	/** 启动时间。在构造时生成，不能修改 **/
	private long runTime;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(naming);
		boolean success = (host != null);
		writer.writeBoolean(success);
		if (success) {
			writer.writeObject(host);
		}
		writer.writeLong(processId);
		writer.writeLong(runTime);
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
		boolean success = reader.readBoolean();
		if (success) {
			host = new SocketHost(reader);
		}
		processId = reader.readLong();
		runTime = reader.readLong();
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入对象生成它的数据副本
	 * @param that TubTubItem实例
	 */
	private TubServiceItem(TubServiceItem that) {
		this();
		naming = that.naming.duplicate();
		if (that.host != null) {
			host = that.host.duplicate();
		}
		processId = that.processId;
		runTime = that.runTime;
	}

	/**
	 * 构造默认的运行中的边缘容器单元
	 */
	public TubServiceItem() {
		super();
		processId = -1;
		runTime = 0;
	}

	/**
	 * 从可类化数据读取器中解析运行中的边缘容器单元参数
	 * @param reader 可类化数据读取器
	 */
	public TubServiceItem(ClassReader reader) {
		this();
		resolve(reader);
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
	public long getRunTime() {
		return runTime;
	}

	/**
	 * 返回任务运行时间
	 * @return 调用器运行时间
	 */
	public void setRunTime(long who) {
		runTime = who;
	}

	/**
	 * 返回运行中的边缘容器单元的数据副本
	 * @return TubServiceItem实例
	 */
	public TubServiceItem duplicate() {
		return new TubServiceItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != TubServiceItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((TubServiceItem) that) == 0;
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
	 * 比较两个运行中的边缘容器单元的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TubServiceItem that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		// 名称是唯一判断标准
		return Laxkit.compareTo(processId, that.processId);
	}


}