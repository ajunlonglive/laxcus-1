/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * 用户基点 <br>
 * 
 * 由用户签名和站点地址组成，描述注册用户和他所处的站点，以此判断唯一性。
 * 
 * @author scott.liang
 * @version 1.1 11/15/2015
 * @since laxcus 1.0
 */
public final class Seat implements Serializable, Cloneable, Classable, Markable, Comparable<Seat> {

	private static final long serialVersionUID = -2490446270326685614L;

	/** 用户签名 */
	private Siger siger;

	/** 注册地址 */
	private Node site;

	/** 用户名称的明文。默认是空 **/
	private String plainText;

	/**
	 * 根据传入对象生成它的数据副本
	 * @param that Seat实例
	 */
	protected Seat(Seat that) {
		this();
		siger = that.siger.duplicate();
		site = that.site.duplicate();
		plainText = that.plainText;
	}

	/**
	 * 构造默认的用户基点
	 */
	protected Seat() {
		super();
	}

	/**
	 * 构造用户基点，指定用户签名和站点地址
	 * @param siger 用户签名
	 * @param node 站点地址
	 */
	public Seat(Siger siger, Node node) {
		this();
		setSiger(siger);
		setSite(node);
	}

	/**
	 * 构造用户基点，指定站点地址和用户签名
	 * @param node 站点地址
	 * @param siger 用户签名
	 */
	public Seat(Node node, Siger siger) {
		this();
		setSite(node);
		setSiger(siger);
	}

	/**
	 * 从可类化数据读取器中解析用户基点参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Seat(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出用户基点
	 * @param reader 标记化读取器
	 */
	public Seat(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置用户签名
	 * @param e Siger实例
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);

		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 设置站点地址
	 * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);

		site = e;
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}


	/**
	 * 设置用户名称的明文，允许空指针
	 * @param e 明文
	 */
	public void setPlainText(String e) {
		plainText = e;
	}

	/**
	 * 返回用户名称的明文
	 * @return 字符串
	 */
	public String getPlainText() {
		return plainText;
	}

	/**
	 * 返回当前用户基点的数据副本
	 * @return 新的Seat实例
	 */
	public Seat duplicate() {
		return new Seat(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Seat.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Seat) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode() ^ site.hashCode();
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
		return String.format("<%s>/%s", siger, site);
	}

	/*
	 * 比较两个用户基点的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Seat that) {
		// 空对象排在前面，有效对象在后面
		if (that == null) {
			return 1;
		}
		int ret = Laxkit.compareTo(siger, that.siger);
		if (ret == 0) {
			ret = Laxkit.compareTo(site, that.site);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(siger);
		writer.writeObject(site);
		writer.writeString(plainText);
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		siger = new Siger(reader);
		site = new Node(reader);
		plainText = reader.readString();
		return reader.getSeek() - seek;
	}

}