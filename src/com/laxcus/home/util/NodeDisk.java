/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.util;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 站点空间。排序按照从小到大排列。
 * 
 * @author scott.liang
 * @version 1.1 03/26/2015
 * @since laxcus 1.0
 */
public final class NodeDisk implements Cloneable, Serializable, Classable, Comparable<NodeDisk> {

	private static final long serialVersionUID = -1600283601106153333L;

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		writer.writeLong(size);
		writer.writeObject(node);
		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		size = reader.readLong();
		node = new Node(reader);
		return reader.getSeek() - scale;
	}

	/** 磁盘空间 **/
	private long size;

	/** 节点 **/
	private Node node;

	/**
	 * 构造默认的站点空间
	 */
	public NodeDisk() {
		super();
	}

	/**
	 * 构造站点空间，指定参数
	 * @param that
	 */
	public NodeDisk(NodeDisk that) {
		this();
		size = that.size;
		node = that.node;
	}

	/**
	 * 构造站点空间，指定参数
	 * @param size 磁盘空间
	 * @param node 节点地址
	 */
	public NodeDisk(long size, Node node) {
		super();
		setSize(size);
		setNode(node);
	}

	/**
	 * 从可类化读器中解析站点空间参数
	 * @param reader 可类化读器
	 * @since 1.1
	 */
	public NodeDisk(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置磁盘尺寸
	 * @param i 磁盘尺寸
	 */
	public void setSize(long i) {
		size = i;
	}

	/**
	 * 返回磁盘尺寸
	 * @return 磁盘尺寸
	 */
	public long getSize() {
		return size;
	}

	/**
	 * 设置节点
	 * @param e Node实例
	 */
	public void setNode(Node e) {
		node = e;
	}

	/**
	 * 返回节点
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != NodeDisk.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((NodeDisk) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (size ^ node.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new NodeDisk(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(NodeDisk that) {
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(size, that.size);
		if (ret == 0) {
			ret = Laxkit.compareTo(node, that.node);
		}
		return ret;
	}

}