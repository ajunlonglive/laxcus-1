/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.site.watch;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 用户资源消耗成员
 * 
 * @author scott.liang
 * @version 1.0 12/19/2020
 * @since laxcus 1.0
 */
public final class UserCostElement implements Classable, Cloneable, Serializable, Comparable<UserCostElement> {

	private static final long serialVersionUID = 5651643178030182283L;

	/** 用户签名 **/
	private Siger issuer;

	/** 未使用容量 **/
	private TreeSet<UserCostItem> array = new TreeSet<UserCostItem>();
	
	/**
	 * 构造默认的用户资源消耗成员
	 */
	public UserCostElement() {
		super();
	}

	/**
	 * 构造用户资源消耗成员
	 * @param issuer 用户签名
	 */
	public UserCostElement(Siger issuer) {
		this();
		setIssuer(issuer);
	}
	
	/**
	 * 根据传入实例，生成用户资源消耗成员的数据副本
	 * @param that UserCostElement实例
	 */
	private UserCostElement(UserCostElement that) {
		super();
		issuer = that.issuer;
		array.addAll(that.array);
	}

	/**
	 * 从可类化数据读取器中用户资源消耗成员
	 * @param reader 可类化数据读取器
	 */
	public UserCostElement(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名
	 * @param who 签名
	 */
	public void setIssuer(Siger who) {
		issuer = who;
	}

	/**
	 * 返回用户签名
	 * @return 签名
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 加一个单元
	 * @param item
	 * @return
	 */
	public boolean add(UserCostItem item) {
		if (item != null) {
			return array.add(item);
		}
		return false;
	}
	
	/**
	 * 增加一批单元
	 * @param a
	 * @return
	 */
	public int addAll(Collection<UserCostItem> a) {
		int size = array.size();
		if (a != null) {
			for (UserCostItem item : a) {
				add(item);
			}
		}
		return array.size() - size;
	}

	/**
	 * 返回单元
	 * @return
	 */
	public List<UserCostItem> list() {
		return new ArrayList<UserCostItem>(array);
	}

	/**
	 * 全部成员
	 * @return
	 */
	public int size() {
		return array.size();
	}
	
	/**
	 * 生成当前实例的数据副本
	 * @return UserCostElement实例
	 */
	public UserCostElement duplicate() {
		return new UserCostElement(this);
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
		return compareTo((UserCostElement) that) == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(UserCostElement that) {
		if (that == null) {
			return 1;
		}
		// 比较参数
		return Laxkit.compareTo(issuer, that.issuer);
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
	 * 保存参数
	 * @param writer 
	 */
	protected void buildSuffix(ClassWriter writer) {
		writer.writeObject(issuer);
		int size = array.size();
		writer.writeInt(size);
		for (UserCostItem e : array) {
			writer.writeObject(e);
		}
	}

	/**
	 * 解析参数
	 * @param reader
	 */
	protected void resolveSuffix(ClassReader reader) {
		issuer = new Siger(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			UserCostItem item = new UserCostItem(reader);
			add(item);
		}
	}

}