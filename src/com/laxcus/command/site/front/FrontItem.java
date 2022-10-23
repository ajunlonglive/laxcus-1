/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * FRONT注册单元
 * 
 * @author scott.liang
 * @version 1.1 09/09/2015
 * @since laxcus 1.0
 */
public class FrontItem implements Classable, Serializable, Cloneable, Comparable<FrontItem> {

	private static final long serialVersionUID = -1770346540306056579L;

	/** 在线用户名 **/
	private Siger username;

	/** FRONT站点登录地址 **/
	private Node front;

	/**
	 * 构造默认和私有的FRONT注册单元
	 */
	private FrontItem() {
		super();
	}

	/**
	 * 生成FRONT注册单元的数据副本
	 * @param that FrontItem实例
	 */
	private FrontItem(FrontItem that) {
		this();
		username = that.username;
		front = that.front;
	}

	/**
	 * 构造FRONT注册单元，指定参数
	 * @param username 用户名
	 * @param front 站点地址
	 */
	public FrontItem(Siger username, Node front) {
		this();
		setUsername(username);
		setFront(front);
	}

	/**
	 * 从可类化数据读取器中解析FRONT注册单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public FrontItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置账号用户名
	 * @param e Siger实例
	 */
	public void setUsername(Siger e) {
		Laxkit.nullabled(e);

		username = e;
	}

	/**
	 * 返回账号用户名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return username;
	}

	/**
	 * 设置FRONT站点地址
	 * @param e Node实例
	 */
	public void setFront(Node e) {
		Laxkit.nullabled(e);

		front = e;
	}
	
	/**
	 * 返回FRONT站点地址
	 * @return Node实例
	 */
	public Node getFront() {
		return front;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return FrontItem实例
	 */
	public FrontItem duplicate() {
		return new FrontItem(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FrontItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((FrontItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return username.hashCode() ^ front.hashCode();
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
	public int compareTo(FrontItem that) {
		if (that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(username, that.username);
		if (ret == 0) {
			ret = Laxkit.compareTo(front, that.front);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(username);
		writer.writeObject(front);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		username = new Siger(reader);
		front = new Node(reader);
		return reader.getSeek() - seek;
	}

}