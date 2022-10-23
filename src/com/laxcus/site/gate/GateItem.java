/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.site.gate;

import java.io.*;
import java.util.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * GATE站点注册用户单元
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class GateItem implements Serializable, Cloneable, Classable, Comparable<GateItem> {

	private static final long serialVersionUID = -2387965502044550133L;

	/** FRONT用户签名 **/
	private Siger siger;

	/** FRONT站点地址 **/
	private TreeSet<Node> fronts = new TreeSet<Node>();

	/**
	 * 构造默认的GATE站点注册用户单元
	 */
	private GateItem() {
		super();
	}

	/**
	 * 生成GATE站点注册用户单元的数据副本
	 * @param that GATE站点注册用户单元
	 */
	public GateItem(GateItem that) {
		this();
		if (that.siger != null) {
			siger = that.siger.duplicate();
		}
		fronts.addAll(that.fronts);
	}

	/**
	 * 构造GATE站点注册用户单元，指定参数
	 * @param siger 用户签名
	 * @param fronts FRONT站点地址集合
	 */
	public GateItem(Siger siger, List<Node> fronts) {
		this();
		setSiger(siger);
		addAll(fronts);
	}

	/**
	 * 从可类化数据读取器中解析GATE站点注册用户单元
	 * @param reader 可类化数据读取器
	 */
	public GateItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置用户签名，不允许空指针
	 * @param e
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);
		siger = e;
	}

	/**
	 * 返回用户签名
	 * @return 用户签名
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 保存站点地址，不允许空指针
	 * @param e 站点地址
	 * @return 成功返回真，否则假
	 */
	public boolean add(Node e) {
		Laxkit.nullabled(e);
		return fronts.add(e);
	}

	/**
	 * 保存一批FRONT站点地址
	 * @param a FRONT站点地址集合
	 * @return 返回新增成员数目
	 */
	public int addAll(List<Node> a) {
		int size = fronts.size();
		// 避免空指针
		if (a != null) {
			for (Node e : a) {
				add(e);
			}
		}
		return fronts.size() - size;
	}

	/**
	 * 输出全部FRONT站点地址
	 * @return FRONT站点地址
	 */
	public List<Node> list() {
		return new ArrayList<Node>(fronts);
	}

	/**
	 * 判断用户登录
	 * @return 返回真或者假
	 */
	public boolean isLogined() {
		return fronts.size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != GateItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((GateItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%s", siger, (isLogined() ? "Logined"
				: "Not Login"));
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GateItem that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(siger, that.siger);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(siger);
		writer.writeInt(fronts.size());
		for(Node e : fronts) {
			writer.writeObject(e);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		siger = new Siger(reader);
		int size = reader.readInt();
		for(int i = 0; i <size; i++) {
			Node e = new Node(reader);
			fronts.add(e);
		}
		return reader.getSeek() - seek;
	}

}
