/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.access.user;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.site.*;
import com.laxcus.util.classable.*;

/**
 * 用户资源检索结果单元
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public class SeekUserSiteItem implements Classable, Cloneable, Serializable, Comparable<SeekUserSiteItem> {

	private static final long serialVersionUID = 565513556885406343L;

	/** 用户基础，坐标 **/
	private Seat seat;

	/**
	 * 构造默认的被刷新处理单元
	 */
	protected SeekUserSiteItem() {
		super();
	}

	/**
	 * 根据传入实例，生成用户资源检索结果单元的数据副本
	 * @param that SeekUserSiteItem实例
	 */
	protected SeekUserSiteItem(SeekUserSiteItem that) {
		super();
		seat = that.seat;
	}

	/**
	 * 构造用户资源检索结果单元，指定用户基点
	 * @param seat 用户基点
	 */
	public SeekUserSiteItem(Seat seat) {
		this();
		setSeat(seat);
	}

	/**
	 * 构造用户资源检索结果单元，指定用户基点和站点地址
	 * @param siger 用户基点
	 * @param site 站点地址
	 */
	public SeekUserSiteItem(Siger siger, Node site) {
		this(new Seat(siger, site));
	}

	/**
	 * 从可类化数据读取器中用户资源检索结果单元
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SeekUserSiteItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户基点
	 * @param e Seat实例
	 */
	public void setSeat(Seat e) {
		Laxkit.nullabled(e);

		seat = e;
	}

	/**
	 * 返回用户基点
	 * @return Seat实例
	 */
	public Seat getSeat() {
		return seat;
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getUsername() {
		return seat.getSiger();
	}

	/**
	 * 返回站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return seat.getSite();
	}

	/**
	 * 生成当前实例的数据副本
	 * @return SeekUserSiteItem实例
	 */
	public SeekUserSiteItem duplicate() {
		return new SeekUserSiteItem(this);
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
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((SeekUserSiteItem ) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return seat.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return seat.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SeekUserSiteItem that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		return Laxkit.compareTo(seat, that.seat);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		buildSuffix(writer);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		resolveSuffix(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 保存参数到可类化写入器
	 * @param writer
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(seat);
	}

	/**
	 * 从可类化读取器解析参数
	 * @param reader 可类化读取器
	 */
	protected void resolveSuffix(ClassReader reader) {
		seat = new Seat(reader);
	}
}