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
 * 数据表名集合。<br><br>
 * 
 * 数据表名集合是保存一批数据表名。集合支持同步/非同步两种模式。
 * add、remove、list、contains、clear、size、isEmpty是非同步方法，push、drop、show、exists是同步方法。
 * 非同步方法的名称继承自java.util.Collection命名规范，同步方法的名称是自定义。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class SpaceSet extends MutexHandler implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -2058612986422286782L;

	/** 数据表名集合 */
	private TreeSet<Space> array = new TreeSet<Space>();

	/**
	 * 将数据表名集合写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 写入成员数目
		writer.writeInt(array.size());
		// 写入每一个成员
		for (Space e : array) {
			writer.writeObject(e);
		}
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析数据表名集合
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 读成员数目
		int size = reader.readInt();
		// 读每个成员和保存它
		for (int i = 0; i < size; i++) {
			Space e = new Space(reader);
			array.add(e);
		}
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 根据传入的数据表名集合实例，生成它的副本
	 * @param that SpaceSet实例
	 */
	private SpaceSet(SpaceSet that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 构造一个默认的数据表名集合
	 */
	public SpaceSet() {
		super();
	}

	/**
	 * 构造数据表名集合，保存数据表名数组
	 * @param a 数据表名数组
	 */
	public SpaceSet(Space[] a) {
		this();
		addAll(a);
	}

	/**
	 * 构造数据表名集合
	 * @param a 数据表名集合
	 */
	public SpaceSet(Collection<Space> a) {
		this();
		this.addAll(a);
	}

	/**
	 * 从可类化数据读取器中解析数据表名集合
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SpaceSet(ClassReader reader) {
		this();
		this.resolve(reader);
	}

	/**
	 * 保存一个数据表名
	 * @param e 数据表名
	 * @return 返回真或者假
	 */
	public boolean add(Space e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除一个数据表名
	 * @param e 数据表名
	 * @return 返回真或者假
	 */
	public boolean remove(Space e) {
		return array.remove(e);
	}

	/**
	 * 保存一批保存数据表名
	 * @param that SpaceSet实例
	 * @return 返回新增成员数目
	 */
	public int addAll(SpaceSet that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that.array);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批数据表名
	 * @param a 数据表名数组
	 * @return 返回新增成员数目
	 */
	public int addAll(Space[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批数据表名
	 * @param that 数据表名集合
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<Space> that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that);
		}
		return array.size() - size;
	}

	/**
	 * 判断数据表名存在
	 * @param e 数据表名
	 * @return 返回真或者假
	 */
	public boolean contains(Space e) {
		if (e != null) {
			return array.contains(e);
		}
		return false;
	}

	/**
	 * 以非锁定方式输出全部数据表名
	 * @return Space列表
	 */
	public List<Space> list() {
		return new ArrayList<Space>(array);
	}

	/**
	 * 输出全部数据表名
	 * @return Space集合
	 */
	public Set<Space> set() {
		return new TreeSet<Space>(array);
	}
	
	/**
	 * 清除全部数据表名
	 */
	public void clear() {
		this.array.clear();
	}

	/**
	 * 统计数据表名的数目
	 * @return 表名数目
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
	 * 以非锁定方式输出全部数据表名数组
	 * @return 数据表名数组
	 */
	public Space[] toArray() {
		Space[] a = new Space[array.size()];
		return array.toArray(a);
	}

	/**
	 * 检查两个数据表名集合完全一致
	 * @param that 另一个集合
	 * @return 一致返回真，否则假。
	 */
	public boolean alike(SpaceSet that) {
		// 1. 参数有效
		boolean success = (that != null);
		// 2. 数目一致
		if (success) {
			success = (size() == that.size());
		}
		// 3. 检查每个表都匹配
		if (success) {
			int count = 0;
			for (Space e : that.array) {
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
	 * 克隆当前数据表名集合的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new SpaceSet(this);
	}

	/**
	 * 以锁定方式增加一个数据表名。不允许空指针或者数据表名重叠的现象存在。
	 * @param e 数据表名
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean push(Space e) {
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
	 *以锁定方式删除一个数据表名。
	 * @param e 数据表名
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean drop(Space e) {
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
	 * 以锁定方式增加一组数据表名。每个数据表名都是唯一的，不允许重叠现象存在。
	 * @param a 数据表名数组
	 * @return 返回增加的成员数
	 */
	public int push(Space[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			this.push(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定方式增加一组数据表名
	 * @param a 运行数据表名集合
	 * @return 返回增加的成员数
	 */
	public int push(Collection<Space> a) {
		int size = array.size();
		for (Space e : a) {
			this.push(e);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定的方式删除一组数据表名。
	 * @param a 数据表名集合
	 * @return 返回删除的成员数目
	 */
	public int drop(Collection<Space> a) {
		int size = array.size();
		for (Space e : a) {
			this.drop(e);
		}
		return size - array.size();
	}

	/**
	 * 以锁定方式判断数据表名存在
	 * @param e 数据表名
	 * @return 存在返回真，否则假
	 */
	public boolean exists(Space e) {
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
	 * 以锁定方式输出全部数据表名
	 * @return 数据表名列表
	 */
	public List<Space> show() {
		super.lockMulti();
		try {
			return new ArrayList<Space>(array);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 以锁定方式输出全部数据表名
	 * @return 数据表名数组
	 */
	public Space[] array() {
		super.lockMulti();
		try {
			Space[] all = new Space[array.size()];
			return array.toArray(all);
		} finally {
			super.unlockMulti();
		}
	}
}