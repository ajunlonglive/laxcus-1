/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.sign;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块签名站点
 * 
 * @author scott.liang
 * @version 1.0 9/12/2016
 * @since laxcus 1.0
 */
public final class SignSite implements Classable, Cloneable, Serializable, Comparable<SignSite> {

	private static final long serialVersionUID = -7615838890466738327L;

	/** 节点地址 **/
	private Node node;

	/** 签名表 **/
	private SignTable table;

	/**
	 * 构造默认的数据块签名站点
	 */
	public SignSite() {
		super();
	}

	/**
	 * 构造数据块签名站点，指定节点地址和签名表
	 * @param node  节点地址
	 * @param table  签名表
	 */
	public SignSite(Node node, SignTable table) {
		this();
		setNode(node);
		setTable(table);
	}

	/**
	 * 生成数据块签名站点数据副本
	 * @param that SignSite实例
	 */
	private SignSite(SignSite that) {
		this();
		node = that.node;
		table = that.table;
	}

	/**
	 * 从可类化数据读取器中解析数据块签名站点
	 * @param reader  可类化数据读取器
	 */
	public SignSite(ClassReader reader) {
		this();
		resolve(reader);
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
	 * 判断是主站点
	 * @return 返回真或者假
	 */
	public boolean isMaster() {
		return node.isMaster();
	}

	/**
	 * 判断是从站点
	 * @return 返回真或者假
	 */
	public boolean isSlave() {
		return node.isSlave();
	}

	/**
	 * 设置数据块签名表
	 * @param e SignTable实例
	 */
	public void setTable(SignTable e) {
		Laxkit.nullabled(e);

		table = e;
	}

	/**
	 * 返回数据块签名表
	 * @return SignTable实例
	 */
	public SignTable getTable() {
		return table;
	}

	/**
	 * 返回表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return table.getSpace();
	}

	/**
	 * 生成当前对象的数据副本
	 * @return SignSite实例
	 */
	public SignSite duplicate() {
		return new SignSite(this);
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
		if (that == null || that.getClass() != SignSite.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SignSite) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return node.hashCode() ;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SignSite that) {
		if (that == null) {
			return 1;
		}
		// 比较
		int ret = Laxkit.compareTo(node, that.node);
		if (ret == 0) {
			ret = Laxkit.compareTo(table.getSpace(), that.table.getSpace());
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(node);
		writer.writeObject(table);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		node = new Node(reader);
		table = new SignTable(reader);
		return reader.getSeek() - seek;
	}

}
