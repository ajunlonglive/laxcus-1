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
import com.laxcus.util.classable.*;
import com.laxcus.util.lock.*;
import com.laxcus.util.naming.*;

/**
 * 阶段命名集合。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class PhaseSet extends MutexHandler implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = -1988097094265243304L;

	/** 阶段命名集合 */
	private TreeSet<Phase> array = new TreeSet<Phase>();

	/**
	 * 将阶段命名集合写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 写入成员数目
		writer.writeInt(array.size());
		// 写入每一个成员
		for (Phase e : array) {
			writer.writeObject(e);
		}
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析阶段命名集合
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 读成员数目
		int size = reader.readInt();
		// 读每个成员和保存它
		for (int i = 0; i < size; i++) {
			Phase e = new Phase(reader);
			array.add(e);
		}
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 根据传入的阶段命名集合实例，生成它的副本
	 * @param that PhaseSet实例
	 */
	private PhaseSet(PhaseSet that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 构造一个默认的阶段命名集合
	 */
	public PhaseSet() {
		super();
	}

	/**
	 * 构造阶段命名集合，保存阶段命名数组
	 * @param a 阶段命名数组
	 */
	public PhaseSet(Phase[] a) {
		this();
		addAll(a);
	}

	/**
	 * 构造阶段命名集合
	 * @param a 阶段命名集合
	 */
	public PhaseSet(Collection<Phase> a) {
		this();
		addAll(a);
	}

	/**
	 * 从可类化数据读取器中解析阶段命名集合
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public PhaseSet(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 保存一个阶段命名
	 * @param e 阶段命名
	 * @return 返回真或者假
	 */
	public boolean add(Phase e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除一个阶段命名
	 * @param e 阶段命名
	 * @return 返回真或者假
	 */
	public boolean remove(Phase e) {
		return array.remove(e);
	}

	/**
	 * 保存一批阶段命名
	 * @param that PhaseSet实例
	 * @return 返回新增加的阶段命名数目
	 */
	public int addAll(PhaseSet that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that.array);
		}
		return array.size() - size;
	}

	/**
	 * 保存一组阶段命名
	 * @param a 阶段命名数组
	 * @return 被保存的成员数目
	 */
	public int addAll(Phase[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批阶段命名
	 * @param that 阶段命名集合
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<Phase> that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that);
		}
		return array.size() - size;
	}

	/**
	 * 判断阶段命名存在
	 * @param e 阶段命名
	 * @return 返回真或者假
	 */
	public boolean contains(Phase e) {
		if (e != null) {
			return array.contains(e);
		}
		return false;
	}

	/**
	 * 以非锁定和复制数据副本方式，输出全部阶段命名
	 * @return Phase列表
	 */
	public List<Phase> list() {
		return new ArrayList<Phase>(array);
	}
	
	/**
	 * 输入全部阶段命名
	 * @return Phase集合
	 */
	public Set<Phase> set() {
		return array;
	}

	/**
	 * 清除全部阶段命名
	 */
	public void clear() {
		array.clear();
	}
	
	/**
	 * 统计阶段命名的数目
	 * @return 阶段命名数目
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
	 * 以非锁定方式输出全部阶段命名数组
	 * @return 阶段命名数组
	 */
	public Phase[] toArray() {
		Phase[] a = new Phase[array.size()];
		return array.toArray(a);
	}

	/**
	 * 克隆当前阶段命名集合的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new PhaseSet(this);
	}

	/**
	 * 以锁定方式增加一个阶段命名。不允许空指针或者阶段命名重叠的现象存在。
	 * @param e 阶段命名
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean push(Phase e) {
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
	 *以锁定方式删除一个阶段命名。
	 * @param e 阶段命名
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean drop(Phase e) {
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
	 * 以锁定方式增加一组阶段命名。每个阶段命名都是唯一的，不允许重叠现象存在。
	 * @param a 阶段命名数组
	 * @return 返回增加的成员数
	 */
	public int push(Phase[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			push(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定方式增加一组阶段命名
	 * @param a 运行阶段命名集合
	 * @return 返回增加的成员数
	 */
	public int push(Collection<Phase> a) {
		int size = array.size();
		for (Phase e : a) {
			push(e);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定的方式删除一组阶段命名。
	 * @param a 阶段命名集合
	 * @return 返回删除的成员数目
	 */
	public int drop(Collection<Phase> a) {
		int size = array.size();
		for (Phase e : a) {
			drop(e);
		}
		return size - array.size();
	}

	/**
	 * 以锁定方式判断阶段命名存在
	 * @param e 阶段命名
	 * @return 存在返回真，否则假
	 */
	public boolean exists(Phase e) {
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
	 * 以锁定方式输出全部阶段命名
	 * @return 阶段命名集合
	 */
	public List<Phase> show() {
		super.lockMulti();
		try {
			return new ArrayList<Phase>(array);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 以锁定方式输出全部阶段命名
	 * @return 阶段命名数组
	 */
	public Phase[] array() {
		super.lockMulti();
		try {
			Phase[] a = new Phase[array.size()];
			return array.toArray(a);
		} finally {
			super.unlockMulti();
		}
	}
}