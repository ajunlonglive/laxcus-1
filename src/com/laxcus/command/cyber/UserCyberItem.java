/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.cyber;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.cyber.*;
import com.laxcus.site.*;

/**
 * 用户虚拟空间应答单元
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public final class UserCyberItem implements Serializable, Cloneable, Classable, Comparable<UserCyberItem> {

	private static final long serialVersionUID = 3592227033886532562L;

	/** 站点地址 **/
	private Node site;

	/** 瞬时记录 **/
	private Moment moment;

	/**
	 * 构造默认和私有的用户虚拟空间应答单元
	 */
	private UserCyberItem() {
		super();
	}

	/**
	 * 生成用户虚拟空间应答单元数据副本
	 * @param that 原本
	 */
	private UserCyberItem(UserCyberItem that) {
		site = that.site.duplicate();
		moment = that.moment.duplicate();
	}

	/**
	 * 构造用户虚拟空间应答单元，指定站点地址和瞬时记录
	 * @param node 站点地址
	 * @param successful 瞬时记录
	 */
	public UserCyberItem(Node node, Moment successful) {
		this();
		setSite(node);
		setMoment(successful);
	}

	/**
	 * 从可类化数据读取器解析用户虚拟空间应答单元
	 * @param reader 可类化数据读取器
	 */
	public UserCyberItem(ClassReader reader) {
		this();
		resolve(reader);
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
	 * 设置瞬时记录
	 * @param e 瞬时记录
	 */
	public void setMoment(Moment e) {
		moment = e;
	}

	/**
	 * 返回瞬时记录
	 * @return Moment实例
	 */
	public Moment getMoment() {
		return moment;
	}

	/**
	 * 产生数据副本
	 * @return UserCyberItem实例
	 */
	public UserCyberItem duplicate() {
		return new UserCyberItem(this);
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
		return String.format("%s", site);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != UserCyberItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		// 比较
		return compareTo((UserCyberItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return site.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(UserCyberItem that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(site, that.site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(site);
		writer.writeInstance(moment);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		site = new Node(reader);
		moment = reader.readInstance(Moment.class);
		return reader.getSeek() - seek;
	}

}