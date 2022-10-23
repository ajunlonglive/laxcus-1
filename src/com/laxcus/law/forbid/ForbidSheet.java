/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.forbid;

import java.io.*;
import java.util.*;

import com.laxcus.law.rule.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 禁止操作集合。<br>
 * 
 * 管理运行中的禁止操作。
 * 
 * @author scott.liang
 * @version 1.0 3/25/2017
 * @since laxcus 1.0
 */
public final class ForbidSheet implements Classable, Cloneable, Comparable<ForbidSheet>, Serializable {

	private static final long serialVersionUID = 1920177966175272945L;

	/** 用户签名 **/
	private Siger issuer;

	/** 禁止操作单元集合 **/
	private TreeSet<ForbidItem> array = new TreeSet<ForbidItem>();

	/**
	 * 构造默认的禁止操作集合
	 */
	public ForbidSheet() {
		super();
	}

	/**
	 * 构造禁止操作集合，指定用户签名
	 * @param issuer 用户签名
	 */
	public ForbidSheet(Siger issuer) {
		this();
		setIssuer(issuer);
	}
	
	/**
	 * 构造禁止操作表，指定一个禁止操作单元
	 * @param item 禁止操作单元
	 */
	public ForbidSheet(ForbidItem item) {
		this();
		add(item);
	}

	/**
	 * 构造禁止操作表，指定一批禁止操作单元
	 * @param items 禁止操作单元数组
	 */
	public ForbidSheet(ForbidItem[] items) {
		this();
		addAll(items);
	}

	/**
	 * 构造禁止操作表，指定一批禁止操作单元
	 * @param items 禁止操作单元列表
	 */
	public ForbidSheet(List<ForbidItem> items) {
		this();
		addAll(items);
	}

	/**
	 * 从可类化数据读取器中解析禁止操作集合
	 * @param reader 可类化读取器
	 */
	public ForbidSheet(ClassReader reader) {
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
	 * 保存一个禁止操作单元
	 * @param e ForbidItem实例
	 * @return 返回真或者假
	 */
	public boolean add(ForbidItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 增加一批禁止操作单元
	 * @param a 禁止操作单元集合
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<ForbidItem> a) {
		int size = array.size();
		for (ForbidItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 增加一批禁止操作单元
	 * @param that 禁止操作表
	 * @return 返回新增加的成员数目
	 */
	public int addAll(ForbidSheet that) {
		return addAll(that.array);
	}

	/**
	 * 保存一组禁止操作单元
	 * @param a 禁止操作单元列表
	 * @return 返回新增的事务成员数目
	 */
	public int addAll(ForbidItem[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 删除一个禁止操作单元
	 * @param e ForbidItem实例
	 * @return 返回真或者假
	 */
	public boolean remove(ForbidItem e) {
		if (e != null) {
			return array.remove(e);
		}
		return false;
	}

	/**
	 * 删除全部禁止操作单元
	 * @param a 禁止操作单元数组
	 * @return 返回被删除的成员数目
	 */
	public int removeAll(Collection<ForbidItem> a) {
		int size = array.size();
		for (ForbidItem e : a) {
			remove(e);
		}
		return size - array.size();
	}

	/**
	 * 输出全部禁止操作单元
	 * @return ForbidItem列表
	 */
	public List<ForbidItem> list() {
		return new ArrayList<ForbidItem>(array);
	}

	/**
	 * 判断包含一个禁止操作单元
	 * @param e ForbidItem实例
	 * @return 返回真或者假
	 */
	public boolean contains(ForbidItem e) {
		return array.contains(e);
	}
	
	/**
	 * 返回禁止操作单元的迭代器
	 * @return ForbidItem迭代
	 */
	public Iterator<ForbidItem> iterator() {
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
	 * @return 成员数目的整数值
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

	/**
	 * 判断存在冲突
	 * @param rule 事务规则
	 * @return 返回真或者假
	 */
	public boolean conflict(RuleItem rule) {
		ForbidItem item = rule.createForbidItem();
		return conflict(item);
	}

	/**
	 * 判断存在冲突
	 * @param that 禁止操作单元
	 * @return 返回真或者假
	 */
	public boolean conflict(ForbidItem that) {
		for (ForbidItem item : array) {
			if (item.conflict(that)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ForbidSheet that) {
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
		for (ForbidItem item : array) {
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
			ForbidItem item = ForbidItemCreator.resolve(reader);
			array.add(item);
		}
		return reader.getSeek() - seek;
	}

}