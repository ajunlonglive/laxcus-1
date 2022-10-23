/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.distribute.calculate.cyber;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 分布数据映像标识。<br>
 * 以数据源节点地址和任务编号为标识，成为分布数据的指示坐标。
 * 
 * @author scott.liang
 * @version 1.1 03/18/2015
 * @since laxcus 1.0
 */
public final class CyberFlag implements Classable, Serializable, Cloneable, Comparable<CyberFlag> {

	private static final long serialVersionUID = 5395936730465471769L;

	/** 数据源节点地址 **/
	private Node node;

	/** 任务编号 **/
	private long taskId;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(node);
		writer.writeLong(taskId);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		node = new Node(reader);
		taskId = reader.readLong();
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的CyberFlag参数，生成它的副本
	 * @param that CyberFlag实例
	 */
	private CyberFlag(CyberFlag that) {
		super();
		node = that.node.duplicate();
		taskId = that.taskId;
	}

	/**
	 * 构造CyberFlag，同时指定它的数据源头地址和任务编号
	 * @param node 数据源头地址(DATA/WORK)
	 * @param taskId 任务编号
	 */
	public CyberFlag(Node node, long taskId) {
		super();
		setNode(node);
		setTaskId(taskId);
	}

	/**
	 * 从可类化数据读取器中解析CyberFlag
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public CyberFlag(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 设置数据源节点地址
	 * @param e Node实例
	 */
	public void setNode(Node e) {
		node = e;
	}

	/**
	 * 返回数据源节点地址
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 设置任务编号
	 * 
	 * @param i 任务编号
	 */
	public void setTaskId(long i) {
		taskId = i;
	}

	/**
	 * 返回任务编号
	 * @return 任务编号
	 */
	public long getTaskId() {
		return taskId;
	}

	/**
	 * 返回当前对象的数据副本
	 * @return CyberFlag实例
	 */
	public CyberFlag duplicate() {
		return new CyberFlag(this);
	}

	/**
	 * 比较两个CyberFlag参数是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != CyberFlag.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((CyberFlag) that) == 0;
	}

	/**
	 * 散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (node.hashCode() ^ taskId);
	}

	/**
	 * 根据当前CyberFlag参数，生成它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s/%d", node, taskId);
	}

	/**
	 * 比较CyberFlag的前后顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CyberFlag that) {
		// 空对象在前面
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