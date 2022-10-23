/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.set;

import java.util.*;
import java.io.*;

import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.lock.*;

/**
 * 数字签名人集合。
 * 
 * @author scott.liang
 * @version 1.0 11/17/2015
 * @since laxcus 1.0
 */
public final class SigerSet extends MutexHandler implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -4219723784326201325L;
	
	/** 数字签名人集合 */
	private TreeSet<Siger> array = new TreeSet<Siger>();

	/**
	 * 将数字签名人集合写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 写入成员数目
		writer.writeInt(array.size());
		// 写入每一个成员
		for (Siger e : array) {
			writer.writeObject(e);
		}
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析数字签名人集合
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 读成员数目
		int size = reader.readInt();
		// 读每个成员和保存它
		for (int i = 0; i < size; i++) {
			Siger e = new Siger(reader);
			array.add(e);
		}
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 根据传入的数字签名人集合实例，生成它的副本
	 * @param that SigerSet实例
	 */
	private SigerSet(SigerSet that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 构造一个默认的数字签名人集合
	 */
	public SigerSet() {
		super();
	}

	/**
	 * 构造数字签名人集合，保存数字签名人数组
	 * @param a 数字签名人数组
	 */
	public SigerSet(Siger[] a) {
		this();
		addAll(a);
	}

	/**
	 * 构造数字签名人集合
	 * @param a 数字签名人集合
	 */
	public SigerSet(Collection<Siger> a) {
		this();
		addAll(a);
	}

	/**
	 * 从可类化数据读取器中解析数字签名人集合
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SigerSet(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个数字签名人
	 * @param e 数字签名人
	 * @return 返回真或者假
	 */
	public boolean add(Siger e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除一个数字签名人
	 * @param e 数字签名人
	 * @return 返回真或者假
	 */
	public boolean remove(Siger e) {
		return array.remove(e);
	}

	/**
	 * 保存一批数字签名人
	 * @param that SigerSet实例
	 * @return 返回新增加的数字签名人数目
	 */
	public int addAll(SigerSet that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that.array);
		}
		return array.size() - size;
	}

	/**
	 * 保存一组数字签名人
	 * @param a 数字签名人数组
	 * @return 被保存的成员数目
	 */
	public int addAll(Siger[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批数字签名人
	 * @param that 数字签名人集合
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<Siger> that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that);
		}
		return array.size() - size;
	}

	/**
	 * 判断数字签名人存在
	 * @param e 数字签名人
	 * @return 返回真或者假
	 */
	public boolean contains(Siger e) {
		if (e != null) {
			return array.contains(e);
		}
		return false;
	}

	/**
	 * 以非锁定和复制数据副本方式，输出全部数字签名人
	 * @return Siger列表
	 */
	public List<Siger> list() {
		return new ArrayList<Siger>(array);
	}
	
	/**
	 * 输入全部数字签名人
	 * @return Siger集合
	 */
	public Set<Siger> set() {
		return array;
	}

	/**
	 * 清除全部数字签名人
	 */
	public void clear() {
		array.clear();
	}
	
	/**
	 * 统计数字签名人的数目
	 * @return 数字签名人数目
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 检测集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 以非锁定方式输出全部数字签名人数组
	 * @return 数字签名人数组
	 */
	public Siger[] toArray() {
		Siger[] a = new Siger[array.size()];
		return array.toArray(a);
	}

	/**
	 * 克隆当前数字签名人集合的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new SigerSet(this);
	}

	/**
	 * 以锁定方式增加一个数字签名人。不允许空指针或者数字签名人重叠的现象存在。
	 * @param e 数字签名人
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean push(Siger e) {
		boolean success = (e != null);
		super.lockSingle();
		try {
			if (success) {
				success = array.add(e);
			}
		} catch (Throwable b) {
			Logger.fatal(b);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 *以锁定方式删除一个数字签名人。
	 * @param e 数字签名人
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean drop(Siger e) {
		boolean success = (e != null);
		super.lockSingle();
		try {
			if (success) {
				success = array.remove(e);
			}
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	/**
	 * 以锁定方式增加一组数字签名人。每个数字签名人都是唯一的，不允许重叠现象存在。
	 * @param a 数字签名人数组
	 * @return 返回增加的成员数
	 */
	public int push(Siger[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			push(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定方式增加一组数字签名人
	 * @param a 数字签名人集合
	 * @return 返回增加的成员数
	 */
	public int push(Collection<Siger> a) {
		int size = array.size();
		for (Siger e : a) {
			push(e);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定的方式删除一组数字签名人。
	 * @param a 数字签名人集合
	 * @return 返回删除的成员数目
	 */
	public int drop(Collection<Siger> a) {
		int size = array.size();
		for (Siger e : a) {
			drop(e);
		}
		return size - array.size();
	}

	/**
	 * 以锁定方式判断数字签名人存在
	 * @param e 数字签名人
	 * @return 存在返回真，否则假
	 */
	public boolean exists(Siger e) {
		boolean success = (e != null);
		super.lockMulti();
		try {
			if (success) {
				success = array.contains(e);
			}
		} finally {
			super.unlockMulti();
		}
		return success;
	}

	/**
	 * 以锁定方式输出全部数字签名人
	 * @return 数字签名人集合
	 */
	public List<Siger> show() {
		super.lockMulti();
		try {
			return new ArrayList<Siger>(array);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 以锁定方式输出全部数字签名人
	 * @return 数字签名人数组
	 */
	public Siger[] array() {
		super.lockMulti();
		try {
			Siger[] a = new Siger[array.size()];
			return array.toArray(a);
		} finally {
			super.unlockMulti();
		}
	}
}