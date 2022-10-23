/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.reload;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 释放节点内存处理单元
 * 
 * @author scott.liang
 * @version 1.0 1/12/2019
 * @since laxcus 1.0
 */
public final class ReplyPacketSizeItem implements Classable, Cloneable, Serializable, Comparable<ReplyPacketSizeItem> {

	private static final long serialVersionUID = 3961364878896709664L;

	/** 站点地址 **/
	private Node node;

	/** 成功标识 **/
	private boolean successful;
	
	/** FIXP UDP包尺寸 **/
	private int packetSize;
	
	/** FIXP 子包UDP尺寸 **/
	private int subPacketSize;

	/**
	 * 构造默认的被刷新处理单元
	 */
	public ReplyPacketSizeItem() {
		super();
		successful = false;
		packetSize = 0;
		subPacketSize = 0;
	}

	/**
	 * 根据传入实例，生成释放节点内存处理单元的数据副本
	 * @param that ReplyPacketSizeItem实例
	 */
	private ReplyPacketSizeItem(ReplyPacketSizeItem that) {
		super();
		node = that.node;
		successful = that.successful;
		packetSize = that.packetSize;
		subPacketSize = that.subPacketSize;
	}

	/**
	 * 构造释放节点内存处理单元，指定站点地址和处理结果
	 * @param node 站点地址
	 * @param successful 成功
	 */
	public ReplyPacketSizeItem(Node node, boolean successful) {
		this();
		setSite(node);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中释放节点内存处理单元
	 * @param reader 可类化数据读取器
	 */
	public ReplyPacketSizeItem(ClassReader reader) {
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
	 * 设置FIXP包尺寸
	 * @param len 整数
	 */
	public void setPacketSize(int len) {
		packetSize = len;
	}
	
	/**
	 * 返回FIXP包尺寸
	 * @return 整数
	 */
	public int getPacketSize() {
		return packetSize;
	}
	
	/**
	 * 设置FIXP子包尺寸
	 * @param len 整数
	 */
	public void setSubPacketSize(int len) {
		subPacketSize = len;
	}
	
	/**
	 * 返回FIXP子包尺寸
	 * @return 整数
	 */
	public int getSubPacketSize() {
		return subPacketSize;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return ReplyPacketSizeItem实例
	 */
	public ReplyPacketSizeItem duplicate() {
		return new ReplyPacketSizeItem(this);
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
		return compareTo((ReplyPacketSizeItem ) that) == 0;
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
	public int compareTo(ReplyPacketSizeItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		return Laxkit.compareTo(node, that.node);
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
		writer.writeInt(packetSize);
		writer.writeInt(subPacketSize);
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		node = new Node(reader);
		successful = reader.readBoolean();
		packetSize = reader.readInt();
		subPacketSize = reader.readInt();
	}
}