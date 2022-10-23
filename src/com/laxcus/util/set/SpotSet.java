/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.set;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.log.client.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.lock.*;

/**
 * 表基点集合。<br><br>
 * 
 * 表基点集合是保存一批表基点。集合支持同步/非同步两种模式。
 * add、remove、list、contains、clear、size、isEmpty是非同步方法，push、drop、show、exists是同步方法。
 * 非同步方法的名称继承自java.util.Collection命名规范，同步方法的名称是自定义。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class SpotSet extends MutexHandler implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 5759691680429498837L;
	
	/** 表基点集合 */
	private TreeSet<Spot> array = new TreeSet<Spot>();

	/**
	 * 将表基点集合写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 写入成员数目
		writer.writeInt(array.size());
		// 写入每一个成员
		for (Spot e : array) {
			writer.writeObject(e);
		}
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析表基点集合
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 读成员数目
		int size = reader.readInt();
		// 读每个成员和保存它
		for (int i = 0; i < size; i++) {
			Spot e = new Spot(reader);
			this.array.add(e);
		}
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 根据传入的表基点集合实例，生成它的副本
	 * @param that SpotSet实例
	 */
	private SpotSet(SpotSet that) {
		this();
		this.addAll(that);
	}

	/**
	 * 构造一个默认的表基点集合
	 */
	public SpotSet() {
		super();
	}

	/**
	 * 构造表基点集合，保存表基点数组
	 * @param a 表基点数组
	 */
	public SpotSet(Spot[] a) {
		this();
		this.addAll(a);
	}

	/**
	 * 构造表基点集合
	 * @param a 表基点集合
	 */
	public SpotSet(Collection<Spot> a) {
		this();
		this.addAll(a);
	}

	/**
	 * 从可类化数据读取器中解析表基点集合
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SpotSet(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 保存一个表基点
	 * @param e 表基点
	 * @return 返回真或者假
	 */
	public boolean add(Spot e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除一个表基点
	 * @param e 表基点
	 * @return 返回真或者假
	 */
	public boolean remove(Spot e) {
		return array.remove(e);
	}

	/**
	 * 保存一批保存表基点
	 * @param that SportSet实例
	 * @return 返回新增成员数目
	 */
	public int addAll(SpotSet that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that.array);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批表基点
	 * @param a 表基点数组
	 * @return 已经保存的成员数目
	 */
	public int addAll(Spot[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批表基点
	 * @param that 表基点集合
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<Spot> that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that);
		}
		return array.size() - size;
	}

	/**
	 * 判断表基点存在
	 * @param e 表基点
	 * @return 返回真或者假
	 */
	public boolean contains(Spot e) {
		if (e != null) {
			return array.contains(e);
		}
		return false;
	}

	/**
	 * 以非锁定方式输出全部表基点
	 * @return Spot列表
	 */
	public List<Spot> list() {
		return new ArrayList<Spot>(array);
	}

	/**
	 * 输出全部表基点
	 * @return Spot集合
	 */
	public Set<Spot> set() {
		return array;
	}
	
	/**
	 * 清除全部表基点
	 */
	public void clear() {
		this.array.clear();
	}

	/**
	 * 统计表基点的数目
	 * @return 表基点数目
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
	 * 以非锁定方式输出全部表基点数组
	 * @return 表基点数组
	 */
	public Spot[] toArray() {
		Spot[] a = new Spot[array.size()];
		return array.toArray(a);
	}

	/**
	 * 检查两个表基点集合完全一致
	 * @param that 另一个集合
	 * @return 一致返回真，否则假。
	 */
	public boolean alike(SpotSet that) {
		// 1. 参数有效
		boolean success = (that != null);
		// 2. 数目一致
		if (success) {
			success = (size() == that.size());
		}
		// 3. 检查每个表都匹配
		if (success) {
			int count = 0;
			for (Spot e : that.array) {
				if (contains(e)) {
					count++;
				}
			}
			success = (count == size());
		}
		// 返回匹配结果
		return success;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return SpotSet实例
	 */
	public SpotSet duplicate() {
		return new SpotSet(this);
	}

	/**
	 * 克隆当前表基点集合的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 以锁定方式增加一个表基点。不允许空指针或者表基点重叠的现象存在。
	 * @param e 表基点
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean push(Spot e) {
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
	 *以锁定方式删除一个表基点。
	 * @param e 表基点
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean drop(Spot e) {
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
	 * 以锁定方式增加一组表基点。每个表基点都是唯一的，不允许重叠现象存在。
	 * @param a 表基点数组
	 * @return 返回增加的成员数
	 */
	public int push(Spot[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			this.push(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定方式增加一组表基点
	 * @param a 运行表基点集合
	 * @return 返回增加的成员数
	 */
	public int push(Collection<Spot> a) {
		int size = array.size();
		for (Spot e : a) {
			this.push(e);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定的方式删除一组表基点。
	 * @param a 表基点集合
	 * @return 返回删除的成员数目
	 */
	public int drop(Collection<Spot> a) {
		int size = array.size();
		for (Spot e : a) {
			this.drop(e);
		}
		return size - array.size();
	}

	/**
	 * 以锁定方式判断表基点存在
	 * @param e 表基点
	 * @return 存在返回真，否则假
	 */
	public boolean exists(Spot e) {
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
	 * 以锁定方式输出全部表基点
	 * @return 表基点集合
	 */
	public List<Spot> show() {
		super.lockMulti();
		try {
			return new ArrayList<Spot>(array);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 以锁定方式输出全部表基点
	 * @return 表基点数组
	 */
	public Spot[] array() {
		super.lockMulti();
		try {
			Spot[] all = new Spot[array.size()];
			return array.toArray(all);
		} finally {
			super.unlockMulti();
		}
	}
}