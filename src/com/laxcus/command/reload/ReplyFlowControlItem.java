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
 * 据流接收队列成员处理单元
 * 
 * @author scott.liang
 * @version 1.0 9/9/2020
 * @since laxcus 1.0
 */
public final class ReplyFlowControlItem implements Classable, Cloneable, Serializable, Comparable<ReplyFlowControlItem> {

	private static final long serialVersionUID = -1726172724920190056L;

	/** 站点地址 **/
	private Node node;

	/** 成功标识 **/
	private boolean successful;

	/** UDP数据分割块 **/
	private int block;

	/** 分配SOCKET读取一个包的时间 **/
	private int timeslice;
	
	/** 子包内容尺寸 **/
	private int subPacketContentSize;
	
	/**
	 * 构造默认的被刷新处理单元
	 */
	public ReplyFlowControlItem() {
		super();
		successful = false;
		block = 0;
		timeslice = 0;
		subPacketContentSize = 0;
	}

	/**
	 * 根据传入实例，生成据流接收队列成员处理单元的数据副本
	 * @param that ReplyFlowControlItem实例
	 */
	private ReplyFlowControlItem(ReplyFlowControlItem that) {
		super();
		node = that.node;
		successful = that.successful;
		block = that.block;
		timeslice = that.timeslice;
		subPacketContentSize = that.subPacketContentSize;
	}

	/**
	 * 构造据流接收队列成员处理单元，指定站点地址和处理结果
	 * @param node 站点地址
	 * @param successful 成功
	 */
	public ReplyFlowControlItem(Node node, boolean successful) {
		this();
		setSite(node);
		setSuccessful(successful);
	}

	/**
	 * 从可类化数据读取器中据流接收队列成员处理单元
	 * @param reader 可类化数据读取器
	 */
	public ReplyFlowControlItem(ClassReader reader) {
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
	 * 设置UDP数据分割块
	 * @param what 毫秒
	 */
	public void setBlock(int what) {
		block = what;
	}

	/**
	 * 返回UDP数据分割块
	 * @return 毫秒
	 */
	public long getBlock() {
		return block;
	}

	/**
	 * 设置UDP SOCKET读一个UDP包和分发的处理时间
	 * @param ns 微秒级
	 */
	public void setTimeslice(int ns) {
		timeslice = ns;
	}

	/**
	 * 返回UDP SOCKET读一个UDP包和分发的处理时间
	 * @return 微秒级
	 */
	public int getTimeslice() {
		return timeslice;
	}
	
	/**
	 * 设置子包内容尺寸
	 * @param len
	 */
	public void setSubPacketContentSize(int len) {
		subPacketContentSize = len;
	}

	/**
	 * 返回子包内容尺寸
	 * @return 整数
	 */
	public int getSubPacketContentSize() {
		return subPacketContentSize;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return ReplyFlowControlItem实例
	 */
	public ReplyFlowControlItem duplicate() {
		return new ReplyFlowControlItem(this);
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
		return compareTo((ReplyFlowControlItem ) that) == 0;
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
	public int compareTo(ReplyFlowControlItem that) {
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
		writer.writeInt(block);
		writer.writeInt(timeslice);
		writer.writeInt(subPacketContentSize);
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		node = new Node(reader);
		successful = reader.readBoolean();
		block = reader.readInt();
		timeslice = reader.readInt();
		subPacketContentSize = reader.readInt();
	}
}