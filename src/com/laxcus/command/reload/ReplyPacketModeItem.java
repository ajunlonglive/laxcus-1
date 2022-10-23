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
 * 异步传输模式单元
 * 
 * @author scott.liang
 * @version 1.0 9/3/2020
 * @since laxcus 1.0
 */
public final class ReplyPacketModeItem implements Classable, Cloneable, Serializable, Comparable<ReplyPacketModeItem> {

	private static final long serialVersionUID = 3961364878896709664L;

	/** 站点地址 **/
	private Node node;

	/** 成功标识 **/
	private boolean successful;
	
	/** FIXP UDP包异步传输模式 **/
	private int packetMode;
	
	/**
	 * 构造默认的被刷新处理单元
	 */
	public ReplyPacketModeItem() {
		super();
		packetMode = 0;
		successful = false;
	}

	/**
	 * 根据传入实例，生成异步传输模式单元的数据副本
	 * @param that ReplyPacketModeItem实例
	 */
	private ReplyPacketModeItem(ReplyPacketModeItem that) {
		super();
		node = that.node;
		successful = that.successful;
		packetMode = that.packetMode;
	}

	/**
	 * 构造异步传输模式单元，指定站点地址和处理结果
	 * @param node 站点地址
	 * @param successful 成功
	 */
	public ReplyPacketModeItem(Node node, boolean successful) {
		this();
		setSite(node);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中异步传输模式单元
	 * @param reader 可类化数据读取器
	 */
	public ReplyPacketModeItem(ClassReader reader) {
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
	 * 设置FIXP包异步传输模式
	 * @param len 整数
	 */
	public void setPacketMode(int len) {
		packetMode = len;
	}
	
	/**
	 * 返回FIXP包异步传输模式
	 * @return 整数
	 */
	public int getPacketMode() {
		return packetMode;
	}
	

	/**
	 * 生成当前实例的数据副本
	 * @return ReplyPacketModeItem实例
	 */
	public ReplyPacketModeItem duplicate() {
		return new ReplyPacketModeItem(this);
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
		return compareTo((ReplyPacketModeItem ) that) == 0;
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
	public int compareTo(ReplyPacketModeItem that) {
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
		writer.writeInt(packetMode);
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		node = new Node(reader);
		successful = reader.readBoolean();
		packetMode = reader.readInt();
	}
}