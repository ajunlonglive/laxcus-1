/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.front;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * FRONT登录的详细记录
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class FrontDetail implements Classable, Cloneable, Serializable, Comparable<FrontDetail> {

	private static final long serialVersionUID = -6484151356167523116L;

	/** GATE站点地址 **/
	private Node local;

	/** 注册成员 **/
	private TreeSet<FrontItem> array = new TreeSet<FrontItem>();

	/**
	 * 根据传入的FRONT登录的详细记录，生成它的数据副本
	 * @param that FrontDetail实例
	 */
	private FrontDetail(FrontDetail that) {
		super();
		local = that.local;
		array.addAll(that.array);
	}

	/**
	 * 构造默认的FRONT登录的详细记录
	 */
	public FrontDetail() {
		super();
	}

	/**
	 * 构造构造FRONT登录的详细记录，指定GATE站点地址
	 * @param aid GATE站点地址
	 */
	public FrontDetail(Node aid) {
		this();
		setLocal(aid);
	}

	/**
	 * 从可类化读取器中解析FRONT登录的详细记录
	 * @param reader 可类化数据读取器
	 */
	public FrontDetail(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置GATE站点地址
	 * @param e Node实例
	 */
	public void setLocal(Node e) {
		Laxkit.nullabled(e);
		local = e;
	}

	/**
	 * 返回GATE站点地址
	 * @return Node实例
	 */
	public Node getLocal() {
		return local;
	}

	/**
	 * 保存FRONT注册单元，不允许空指针
	 * @param e FrontItem实例
	 * @return 返回真或者假
	 */
	public boolean add(FrontItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存FRONT注册单元
	 * @param siger 用户签名
	 * @param front FRONT站点
	 * @return 返回真或者假
	 */
	public boolean add(Siger siger, Node front) {
		FrontItem item = new FrontItem(siger, front);
		return add(item);
	}
	
	/**
	 * 输出全部FRONT注册单元
	 * @return FrontItem列表
	 */
	public List<FrontItem> list() {
		return new ArrayList<FrontItem>(array);
	}

	/**
	 * 返回成员数目
	 * @return
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 生成当前实例的数据副本
	 * @return FrontDetail实例
	 */
	public FrontDetail duplicate() {
		return new FrontDetail(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != FrontDetail.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((FrontDetail) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return local.hashCode() ;
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
	public int compareTo(FrontDetail that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(local, that.local);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(local);
		writer.writeInt(array.size());
		for(FrontItem item : array) {
			writer.writeObject(item);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		local = new Node(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			FrontItem item = new FrontItem(reader);
			array.add(item);
		}
		return reader.getSeek() - seek;
	}

}