/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.mix;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 调用器数目单元
 * 
 * @author scott.liang
 * @version 1.0 9/11/2020
 * @since laxcus 1.0
 */
public final class MaxInvokerItem implements Classable, Cloneable, Serializable, Comparable<MaxInvokerItem> {

	private static final long serialVersionUID = -6293384810197643693L;

	/** 站点地址 **/
	private Node node;

	/** 成功标识 **/
	private boolean successful;

	/** 调用器数目 **/
	private int invokers;

	/** 驻时启动 **/
	private long confineTime;
	
	/**
	 * 构造默认的被刷新处理单元
	 */
	public MaxInvokerItem() {
		super();
		successful = false;
		invokers = 0;
		confineTime = 0;
	}

	/**
	 * 根据传入实例，生成调用器数目单元的数据副本
	 * @param that MaxInvokerItem实例
	 */
	private MaxInvokerItem(MaxInvokerItem that) {
		this();
		node = that.node;
		successful = that.successful;
		invokers = that.invokers;
		confineTime = that.confineTime;
	}

	/**
	 * 构造调用器数目单元，指定站点地址和处理结果
	 * @param node 站点地址
	 * @param successful 成功
	 */
	public MaxInvokerItem(Node node, boolean successful) {
		this();
		setSite(node);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中调用器数目单元
	 * @param reader 可类化数据读取器
	 */
	public MaxInvokerItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return node;
	}

	/**
	 * 设置成功标识
	 * @param b 成功标识
	 */
	public void setSuccessful(boolean b) {
		successful = b;
	}

	/**
	 * 判断是成功
	 * @return 返回真或者假
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 设置调用器数目
	 * @param what 毫秒
	 */
	public void setInvokers(int what) {
		invokers = what;
	}

	/**
	 * 返回FIXP失效时间
	 * @return 毫秒
	 */
	public int getInvokers() {
		return invokers;
	}

	/**
	 * 设置调用器限制时间
	 * @param ms 毫秒
	 */
	public void setConfineTime(long ms) {
		confineTime = ms;
	}

	/**
	 * 返回调用器限制时间
	 * @return 毫秒
	 */
	public long getConfineTime() {
		return confineTime;
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return MaxInvokerItem实例
	 */
	public MaxInvokerItem duplicate() {
		return new MaxInvokerItem(this);
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((MaxInvokerItem ) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return node.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", node, (successful ? "Successful" : "Failed"));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MaxInvokerItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		int ret = Laxkit.compareTo(node, that.node);
		if (ret == 0) {
			ret = Laxkit.compareTo(successful, that.successful);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		buildSuffix(writer);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		resolveSuffix(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 保存参数
	 * @param writer 
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(node);
		writer.writeBoolean(successful);
		writer.writeInt(invokers);
		writer.writeLong(confineTime);
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		node = new Node(reader);
		successful = reader.readBoolean();
		invokers = reader.readInt();
		confineTime = reader.readLong();
	}
}