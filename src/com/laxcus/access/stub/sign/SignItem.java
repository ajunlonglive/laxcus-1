/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.sign;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 数据块签名单元，由DATA节点地址和数据块签名组成
 * 
 * @author scott.liang
 * @version 1.0 9/12/2016
 * @since laxcus 1.0
 */
public final class SignItem implements Classable, Cloneable, Serializable, Comparable<SignItem> {

	private static final long serialVersionUID = -7615838890466738327L;

	/** 节点地址 **/
	private Node node;

	/** 数据块签名 **/
	private StubSign sign;

	/**
	 * 构造默认的数据块签名单元
	 */
	public SignItem() {
		super();
	}

	/**
	 * 构造数据块签名单元，指定节点地址和数据块签名
	 * @param node  节点地址
	 * @param sign  数据块签名
	 */
	public SignItem(Node node, StubSign sign) {
		this();
		setNode(node);
		setSign(sign);
	}

	/**
	 * 生成数据块签名单元数据副本
	 * @param that SignItem实例
	 */
	private SignItem(SignItem that) {
		this();
		node = that.node;
		sign = that.sign;
	}

	/**
	 * 从可类化数据读取器中解析数据块签名单元
	 * @param reader  可类化数据读取器
	 */
	public SignItem(ClassReader reader) {
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
	 * 设置数据块签名
	 * @param e 数据块签名实例
	 */
	public void setSign(StubSign e) {
		Laxkit.nullabled(e);

		sign = e;
	}

	/**
	 * 返回数据块签名
	 * @return 数据块签名实例
	 */
	public StubSign getSign() {
		return sign;
	}
	
	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getAddress() {
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
	 * 返回MD5散列码
	 * @return  MD5Hash
	 */
	public MD5Hash getHash() {
		return sign.getHash();
	}
	
	/**
	 * 返回最后修改时间
	 * @return 长整型的修改时间
	 */
	public long getLastModified() {
		return sign.getLastModified();
	}
	
	/**
	 * 判断是存储块状态
	 * @return  返回真或者假
	 */
	public boolean isChunk() {
		return sign.isChunk();
	}

	/**
	 * 判断是缓存块状态
	 * @return  返回真或者假
	 */
	public boolean isCache() {
		return sign.isCache();
	}

	/**
	 * 判断是缓存映像块状态
	 * @return  返回真或者假
	 */
	public boolean isCacheReflex() {
		return sign.isCacheReflex();
	}
	
	/**
	 * 返回当前对象的数据副本
	 * @return SignItem实例
	 */
	public SignItem duplicate() {
		return new SignItem(this);
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
		if (that == null || that.getClass() != SignItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return this.compareTo((SignItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return node.hashCode() ^ sign.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SignItem that) {
		if (that == null) {
			return 1;
		}
		// 比较
		int ret = Laxkit.compareTo(node, that.node);
		if (ret == 0) {
			ret = Laxkit.compareTo(sign, that.sign);
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
		writer.writeObject(sign);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		node = new Node(reader);
		sign = new StubSign(reader);
		return reader.getSeek() - seek;
	}

}
