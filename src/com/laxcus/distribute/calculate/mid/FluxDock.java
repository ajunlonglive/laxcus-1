/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.mid;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * DIFFUSE/CONVERGE计算过程中的中间数据锚点。它由节点地址和中间数据任务编号组成。
 * 
 * @author scott.liang
 * @version 1.1 06/24/2015
 * @since laxcus 1.0
 */
public final class FluxDock implements Classable, Serializable, Cloneable, Comparable<FluxDock> {

	private static final long serialVersionUID = 1998251967296500432L;

	/** 节点地址 **/
	private Node node;

	/** 中间数据任务编号 **/
	private long taskId;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(node);
		writer.writeLong(taskId);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		node = new Node(reader);
		taskId = reader.readLong();
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的中间数据锚点实例，生成它的数据副本
	 * @param that FluxDock实例
	 */
	private FluxDock(FluxDock that) {
		this();
		node = that.node.duplicate();
		taskId = that.taskId;
	}

	/**
	 * 构造默认和私有的中间数据锚点实例
	 */
	private FluxDock() {
		super();
	}

	/**
	 * 构造中间数据锚点实例，指定全部参数
	 * @param node 节点地址
	 * @param taskId 中间数据任务编号
	 */
	public FluxDock(Node node, long taskId) {
		this();
		setNode(node);
		setTaskId(taskId);
	}

	/**
	 * 从可类化读取器中解析并且构造中间数据锚点实例
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FluxDock(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置中间数据节点地址
	 * @param e Node实例
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回中间数据节点地址
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 设置中间数据任务编号
	 * @param i 中间数据任务编号
	 */
	public void setTaskId(long i) {
		taskId = i;
	}

	/**
	 * 返回中间数据任务编号
	 * @return 中间数据任务编号
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * 产生一个当前中间数据锚点实例的数据副本
	 * @return 返回FluxDock实例
	 */
	public FluxDock duplicate() {
		return new FluxDock(this);
	}

	/**
	 * 返回当前实例的字节数组 
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FluxDock.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((FluxDock) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (node.hashCode() ^ taskId);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FluxDock that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(node, that.node);
		if (ret == 0) {
			ret = Laxkit.compareTo(taskId, that.taskId);
		}
		return ret;
	}

}