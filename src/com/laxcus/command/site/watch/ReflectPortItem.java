/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 动态映射端口单元
 * 
 * @author scott.liang
 * @version 1.0 10/22/2020
 * @since laxcus 1.0
 */
public final class ReflectPortItem implements Serializable, Cloneable, Classable, Comparable<ReflectPortItem> {

	private static final long serialVersionUID = -5192073314292722364L;

	/** 
	 * 四种服务器端口，分别是：<br>
	 * 1. 命令流服务器 <br>
	 * 2. 命令包服务器 <br>
	 * 3. 数据流接收服务器 <br>
	 * 4. 数据流发送服务器 <br>
	 */
	public final static int STREAM_SERVER = 1;

	public final static int PACKET_SERVER = 2;

	public final static int SUCKER_SERVER = 3;

	public final static int DISPATCHER_SERVER = 4;

	/** 端口类型 **/
	private int family;
	
	/** 映射端口 **/
	private int port;
	
	/** 成功或者否 **/
	private boolean successful;

	/**
	 * 构造默认和私有的动态映射端口单元
	 */
	private ReflectPortItem() {
		super();
		successful = false;
	}

	/**
	 * 生成动态映射端口单元数据副本
	 * @param that 原本
	 */
	private ReflectPortItem(ReflectPortItem that) {
		this();
		family = that.family;
		port = that.port;
		successful = that.successful;
	}

	/**
	 * 构造动态映射端口单元，指定映射端口和服务器类型
	 * @param family 服务器类型
	 * @param port 映射端口
	 */
	public ReflectPortItem(int family, int port) {
		this();
		setFamily(family);
		setPort(port);
	}

	/**
	 * 从可类化数据读取器解析动态映射端口单元
	 * @param reader 可类化数据读取器
	 */
	public ReflectPortItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置成功或者否
	 * @param b 真或假
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断成功
	 * @return 真或假
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 设置映射端口
	 * @param e int实例
	 */
	public void setPort(int e) {
		Laxkit.nullabled(e);

		port = e;
	}

	/**
	 * 返回映射端口
	 * @return int实例
	 */
	public int getPort() {
		return port;
	}

	/**
	 * 判断是命令流服务器
	 * @return 返回真或者假
	 */
	public boolean isStreamServer() {
		return family == ReflectPortItem.STREAM_SERVER;
	}

	/**
	 * 判断是命令包服务器
	 * @return 返回真或者假
	 */
	public boolean isPacketServer() {
		return family == ReflectPortItem.PACKET_SERVER;
	}

	/**
	 * 判断是数据接收服务器
	 * @return 返回真或者假
	 */
	public boolean isSuckerServer() {
		return family == ReflectPortItem.SUCKER_SERVER;
	}

	/**
	 * 判断是数据发送服务器
	 * @return 返回真或者假
	 */
	public boolean isDispatcherServer() {
		return family == ReflectPortItem.DISPATCHER_SERVER;
	}

	/**
	 * 设置端口类型
	 * @param b 端口类型
	 */
	public void setFamily(int b) {
		switch (b) {
		case ReflectPortItem.STREAM_SERVER:
		case ReflectPortItem.PACKET_SERVER:
		case ReflectPortItem.SUCKER_SERVER:
		case ReflectPortItem.DISPATCHER_SERVER:
			family = b;
			break;
		default:
			throw new IllegalValueException("illegal type:%d", b);
		}
	}

	/**
	 * 判断成功
	 * @return 返回真或者假
	 */
	public int isFamily() {
		return family;
	}

	/**
	 * 产生数据副本
	 * @return ReflectPortItem实例
	 */
	public ReflectPortItem duplicate() {
		return new ReflectPortItem(this);
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
		return String.format("%d/%d", family, port);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ReflectPortItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((ReflectPortItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return family ^ port;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ReflectPortItem that) {
		if (that == null) {
			return 1;
		}
		// 只比较类型
		return Laxkit.compareTo(family, that.family);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeInt(family);
		writer.writeInt(port);
		writer.writeBoolean(successful);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		family = reader.readInt();
		port = reader.readInt();
		successful = reader.readBoolean();
		return reader.getSeek() - seek;
	}

}