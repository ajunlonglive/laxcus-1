/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law.rule;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 事务规则表 <br><br>
 * 
 * 一个事务规则表由任意数量的事务操作单元组成。
 * 
 * @author scott.liang
 * @version 1.11 9/7/2016
 * @since laxcus 1.0
 */
public final class RuleSheet implements Classable, Cloneable, Serializable, Comparable<RuleSheet> {

	private static final long serialVersionUID = 4350890444989102548L;

	/** 规则数组 **/
	private TreeSet<RuleItem> array = new TreeSet<RuleItem>();

	/**
	 * 构造事务规则表的数据副本
	 * @param that RuleSheet实例
	 */
	private RuleSheet(RuleSheet that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 构造默认的事务规则表
	 */
	public RuleSheet() {
		super();
	}

	/**
	 * 构造事务规则表，指定一个事务规则
	 * @param rule 事务规则
	 */
	public RuleSheet(RuleItem rule) {
		this();
		add(rule);
	}

	/**
	 * 构造事务规则表，指定一批事务规则
	 * @param a 事务规则数组
	 */
	public RuleSheet(RuleItem[] a) {
		this();
		addAll(a);
	}

	/**
	 * 构造事务规则表，指定一批事务规则
	 * @param a 事务规则列表
	 */
	public RuleSheet(Collection<RuleItem> a) {
		this();
		addAll(a);
	}

	/**
	 * 从可类化数据读取器中解析事务规则表
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public RuleSheet(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个事务规则。不允许空指针
	 * @param e RuleItem实例
	 */
	public boolean add(RuleItem e) {
		Laxkit.nullabled(e);

		// 判断不存在才保存
		if (!array.contains(e)) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除一个事务规则。不允许空指针
	 * @param e 事务规则
	 * @return 返回真或者假
	 */
	public boolean remove(RuleItem e) {
		Laxkit.nullabled(e);
		// 删除
		return array.remove(e);
	}

	/**
	 * 删除全部事务规则
	 * @param a 事务规则数组
	 * @return 返回被删除的成员数目
	 */
	public int removeAll(Collection<RuleItem> a) {
		int size = array.size();
		for (RuleItem e : a) {
			remove(e);
		}
		return size - array.size();
	}

	/**
	 * 判断指定的事务规则存在
	 * @param e 事务规则
	 * @return 返回真或者假
	 * @since 1.11
	 */
	public boolean contains(RuleItem e) {
		Laxkit.nullabled(e);
		
		return array.contains(e);
	}

	/**
	 * 保存一组事务规则
	 * @param a 事务规则数组
	 * @return 返回新增的事务成员数目
	 */
	public int addAll(Collection<RuleItem> a) {
		int size = array.size();
		for(RuleItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 保存一组事务规则
	 * @param a 事务规则列表
	 * @return 返回新增的事务成员数目
	 */
	public int addAll(RuleItem[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 保存一组事务规则
	 * @param that 事事务规则表
	 * @return 返回新增的事务成员数目
	 */
	public int addAll(RuleSheet that) {
		int size = array.size();
		for (RuleItem e : that.array) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 删除一组事务规则
	 * @param a 事务规则数组
	 * @return 返回删除的事务成员数目
	 */
	public int removeAll(RuleItem[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			remove(a[i]);
		}
		return size - array.size();
	}

	/**
	 * 删除一组事务规则
	 * @param a 事务规则列表
	 * @return 返回删除的事务成员数目
	 */
	public int removeAll(List<RuleItem> a) {
		int size = array.size();
		for (RuleItem e : a) {
			remove(e);
		}
		return size - array.size();
	}

	/**
	 * 删除一组事务规则
	 * @param that 事事务规则表
	 * @return 返回删除的事务成员数目
	 */
	public int removeAll(RuleSheet that) {
		int size = array.size();
		for (RuleItem e : that.array) {
			remove(e);
		}
		return size - array.size();
	}

	/**
	 * 输出全部事务规则单元
	 * @return RuleItem列表
	 */
	public List<RuleItem> list() {
		return new ArrayList<RuleItem>(array);
	}

	/**
	 * 返回事务规则的迭代器
	 * @return RuleItem迭代器
	 */
	public Iterator<RuleItem> iterator() {
		return list().iterator();
	}

	/**
	 * 返回事务规则数目
	 * @return 事务规则数目
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
	 * 两个事务规则表进行比较，判断它们其中的某项规则存在冲突。
	 * 任意一项存在冲突，两个操作不能共享数据资源。
	 * @param that 被比较的事务规则表
	 * @return 存在冲突返回“真”，否则“假”。
	 */
	public boolean conflict(RuleSheet that) {
		// 逐一比较
		for (RuleItem e1 : that.array) {
			for (RuleItem e2 : array) {
				// 任意两项存在冲突，退出，返回“真”。
				if (e1.conflict(e2)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 生成当前事务规则表的数据副本
	 * @return RuleSheet实例
	 */
	public RuleSheet duplicate() {
		return new RuleSheet(this);
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
	public int compareTo(RuleSheet that) {
		if (that == null) {
			return 1;
		}

		Iterator<RuleItem> a1 = iterator();
		Iterator<RuleItem> a2 = that.iterator();

		int ret = 0;
		while (a1.hasNext() && a2.hasNext()) {
			RuleItem e1 = a1.next();
			RuleItem e2 = a2.next();
			ret = e1.compareTo(e2);
			if (ret != 0) break;
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(array.size(), that.array.size());
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeInt(array.size());
		for (RuleItem rule : array) {
			writer.writeObject(rule);
		}
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();

		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			RuleItem rule = RuleItemCreator.resolve(reader);
			if (rule == null) {
				throw new IllegalValueException("illegal rule!");
			}
			array.add(rule);
		}

		return reader.getSeek() - seek;
	}

	/**
	 * 将事务规则表转换为数据流和输出
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		this.build(writer);
		return writer.effuse();
	}

	/**
	 * 解析数据流，转换为事务规则表参数
	 * @param b 字节数组
	 * @param off 字节开始下标
	 * @param len 有效长度
	 * @return 返回解析的字节长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return this.resolve(reader);
	}
}