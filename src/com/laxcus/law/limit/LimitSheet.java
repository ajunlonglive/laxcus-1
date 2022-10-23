/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.limit;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 限制操作集合。<br>
 * 
 * 管理运行中的限制操作。
 * 
 * @author scott.liang
 * @version 1.0 3/25/2017
 * @since laxcus 1.0
 */
public final class LimitSheet implements Classable, Cloneable, Comparable<LimitSheet>, Serializable {

	private static final long serialVersionUID = -6920496133623547013L;

	/** 用户签名 **/
	private Siger issuer;

	/** 限制操作单元集合 **/
	private TreeSet<LimitItem> array = new TreeSet<LimitItem>();

	/**
	 * 构造默认的限制操作集合
	 */
	public LimitSheet() {
		super();
	}
	
	/**
	 * 构造限制操作集合，指定用户签名
	 * @param issuer 用户签名
	 */
	public LimitSheet(Siger issuer) {
		this();
		this.setIssuer(issuer);
	}

	/**
	 * 从可类化数据读取器中解析限制操作集合
	 * @param reader 限制操作
	 */
	public LimitSheet(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置用户签名<br>
	 * @param e Siger实例
	 */
	public void setIssuer(Siger e) {
		Laxkit.nullabled(e);

		issuer = e.duplicate();
	}

	/**
	 * 返回用户签名
	 * @return Siger实例
	 */
	public Siger getIssuer() {
		return issuer;
	}

	/**
	 * 保存一个限制操作单元
	 * @param e LimitItem实例
	 * @return 保存成功返回真，否则假
	 */
	public boolean add(LimitItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 增加一批限制操作单元
	 * @param a LimitItem列表
	 * @return 新增成员数目
	 */
	public int addAll(Collection<LimitItem> a) {
		int size = array.size();
		for (LimitItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 删除一个限制操作单元
	 * @param e LimitItem实例
	 * @return 删除成功返回真，否则假
	 */
	public boolean remove(LimitItem e) {
		if (e != null) {
			return array.remove(e);
		}
		return false;
	}

	/**
	 * 删除全部限制操作单元
	 * @param a 限制操作单元数组
	 * @return 返回被删除的成员数目
	 */
	public int removeAll(Collection<LimitItem> a) {
		int size = array.size();
		for (LimitItem e : a) {
			remove(e);
		}
		return size - array.size();
	}

	/**
	 * 输出全部限制操作单元
	 * @return LimitItem列表
	 */
	public List<LimitItem> list() {
		return new ArrayList<LimitItem>(array);
	}

	/**
	 * 判断包含一个限制操作单元
	 * @param e LimitItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(LimitItem e) {
		return array.contains(e);
	}

	/**
	 * 返回限制操作单元的迭代器
	 * @return LimitItem迭代器
	 */
	public Iterator<LimitItem> iterator() {
		return list().iterator();
	}

	/**
	 * 清除内存记录
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 返回成员数目
	 * @return 成员数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集合
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LimitSheet that) {
		// 排在前面
		if (that == null) {
			return 1;
		}
		// 比较签名
		return Laxkit.compareTo(issuer, that.issuer);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(issuer);
		writer.writeInt(array.size());
		for (LimitItem item : array) {
			writer.writeObject(item);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		issuer = new Siger(reader);
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			LimitItem item = LimitItemCreator.resolve(reader);
			array.add(item);
		}
		return reader.getSeek() - seek;
	}

}
