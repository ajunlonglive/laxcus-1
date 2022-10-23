/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 远端数据表名 <br>
 * 
 * 由数据表名和站点地址组成
 * 
 * @author scott.liang
 * @version 1.0 5/12/2018
 * @since laxcus 1.0
 */
public final class RemoteTableItem implements Serializable, Cloneable, Classable, Markable, Comparable<RemoteTableItem> {

	private static final long serialVersionUID = 5927674944526595271L;

	/** 数据表名 */
	private Space space;

	/** 站点地址 */
	private Node node;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(space);
		writer.writeObject(node);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		space = new Space(reader);
		node = new Node(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入对象生成它的数据副本
	 * @param that RemoteTableItem实例
	 */
	private RemoteTableItem(RemoteTableItem that) {
		this();
		space = that.space.duplicate();
		node = that.node.duplicate();
	}

	/**
	 * 构造默认的远端数据表名
	 */
	private RemoteTableItem() {
		super();
	}

	/**
	 * 构造远端数据表名，指定数据表名和站点地址
	 * @param space 数据表名
	 * @param node 站点地址
	 */
	public RemoteTableItem(Space space, Node node) {
		this();
		setSpace(space);
		setNode(node);
	}

	/**
	 * 从可类化数据读取器中解析远端数据表名参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RemoteTableItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出远端数据表名
	 * @param reader 标记化读取器
	 */
	public RemoteTableItem(MarkReader reader) {
		this();
		reader.readObject(this);
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
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setNode(Node e) {
		Laxkit.nullabled(e);

		node = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * 返回当前远端数据表名的数据副本
	 * @return 新的RemoteTableItem实例
	 */
	public RemoteTableItem duplicate() {
		return new RemoteTableItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != RemoteTableItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((RemoteTableItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return space.hashCode() ^ node.hashCode();
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
		return String.format("<%s>/%s", space, node);
	}

	/*
	 * 比较两个远端数据表名的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RemoteTableItem that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(space, that.space);
		if (ret == 0) {
			ret = Laxkit.compareTo(node, that.node);
		}
		return ret;
	}


}