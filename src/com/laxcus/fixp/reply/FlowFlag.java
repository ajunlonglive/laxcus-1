/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.reply;

import java.io.*;

import com.laxcus.echo.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;
import com.laxcus.util.net.*;

/**
 * 流量控制标记符，包括：<br>
 * 1. 来源地址。<br>
 * 2. 异步通信码。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 12/6/2020
 * @since laxcus 1.0
 */
public final class FlowFlag implements Serializable, Cloneable, Classable, Markable, Comparable<FlowFlag> {

	private static final long serialVersionUID = 2581905647090233410L;

	/** 来源服务器IP地址 **/
	private Address address;

	/** 异步通信码 */
	private CastCode code;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(address);
		writer.writeObject(code);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		address = new Address(reader);
		code = new CastCode(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个默认的流量控制标记符
	 */
	private FlowFlag() {
		super();
	}

	/**
	 * 根据传入的流量控制标记符对象，生成一个它的副本
	 * @param that 流量控制标记符实例
	 */
	private FlowFlag(FlowFlag that) {
		this();
		address = that.address.duplicate();
		code = that.code.duplicate();
	}

	/**
	 * 构造流量控制标记符，并且指定它的来源服务器IP地址和异步通信码
	 * @param address 来源服务器IP地址
	 * @param code 异步通信码
	 */
	public FlowFlag(Address address, CastCode code) {
		this();
		setAddress(address);
		setCode(code);
	}

	/**
	 * 从可类化数据读取器中解析流量控制标记符参数
	 * @param reader 可类化数据读取器
	 */
	public FlowFlag(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出流量控制标记符
	 * @param reader 标记化读取器
	 */
	public FlowFlag(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置来源服务器IP地址。参数必须有效，如果是空值弹出空指针异常。
	 * @param e 来源服务器IP地址
	 * @throws NullPointerException
	 */
	public void setAddress(Address e) {
		Laxkit.nullabled(e);

		address = e;
	}

	/**
	 * 返回来源服务器IP地址
	 * @return Address实例
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * 设置异步通信码
	 * @param e 异步通信码
	 */
	public void setCode(CastCode e) {
		Laxkit.nullabled(e);
		code = e;
	}

	/**
	 * 返回异步通信码
	 * @return 异步通信码
	 */
	public CastCode getCode() {
		return code;
	}

	/**
	 * 返回当前流量控制标记符实例的数据副本
	 * @return FlowFlag实例
	 */
	public FlowFlag duplicate() {
		return new FlowFlag(this);
	}

	/**
	 * 比较两个对象是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FlowFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((FlowFlag) that) == 0;
	}

	/**
	 * 返回散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return address.hashCode() ^ code.hashCode();
	}

	/**
	 * 根据当前实例克隆一个它的数据副本
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
		return String.format("%s/%s", address, code);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FlowFlag that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(address, that.address);
		if (ret == 0) {
			ret = Laxkit.compareTo(code, that.code);
		}
		return ret;
	}

}