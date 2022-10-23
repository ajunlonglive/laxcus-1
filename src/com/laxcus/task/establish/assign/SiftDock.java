/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task.establish.assign;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * SCAN/SIFT计算过程中，SIFT阶段锚点，由BUILD节点地址和数据表名组成。
 * 
 * @author scott.liang
 * @version 1.1 06/24/2015
 * @since laxcus 1.0
 */
public final class SiftDock implements Classable, Serializable, Cloneable, Comparable<SiftDock> {

	private static final long serialVersionUID = -3967503512576306441L;

	/** BUILD节点地址 **/
	private Node node;

	/** 数据表名 **/
	private Space space;

	/**
	 * 根据传入的SIFT锚点实例，生成它的数据副本
	 * @param that SiftDock实例
	 */
	private SiftDock(SiftDock that) {
		this();
		node = that.node.duplicate();
		space = that.space.duplicate();
	}

	/**
	 * 构造默认和私有的SIFT锚点实例
	 */
	private SiftDock() {
		super();
	}

	/**
	 * 构造SIFT锚点实例，指定全部参数
	 * @param node 节点地址
	 * @param space 数据表名
	 */
	public SiftDock(Node node, Space space) {
		this();
		setNode(node);
		setSpace(space);
	}

	/**
	 * 从可类化读取器中解析并且构造SIFT锚点实例
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SiftDock(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置BUILD节点地址
	 * @param e Node实例
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);

		if (!e.isBuild()) {
			throw new IllegalValueException("illegal site:%s", e);
		}
		node = e;
	}

	/**
	 * 返回BUILD节点地址
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 设置数据表名
	 * @param e Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 产生一个当前SIFT锚点实例的数据副本
	 * @return SiftDock
	 */
	public SiftDock duplicate() {
		return new SiftDock(this);
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
		if (that == null || that.getClass() != SiftDock.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SiftDock) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (node.hashCode() ^ space.hashCode());
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
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeObject(node);
		writer.writeObject(space);
		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		node = new Node(reader);
		space = new Space(reader);
		return reader.getSeek() - scale;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SiftDock that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(node, that.node);
		if (ret == 0) {
			ret = Laxkit.compareTo(space, that.space);
		}
		return ret;
	}

}