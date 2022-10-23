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

import com.laxcus.log.client.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.lock.*;

/**
 * 数据块编号集合。<br><br>
 * 
 *“STUB”是数据块编号（CHUNK IDENTITITY）的别名，是一个64位的有符号长整数。
 * 数据块编号由TOP站点产生和分配，在LAXCUS集合中唯一，被DATA/BUILD站点使用，CALL/WORK站点引用它。
 * 每个数据块编号对应一个数据块（CHUNK）。<br><br>
 * 
 * 数据块编号集合提供对数据块编号的各种基础操作。
 * 
 * @author scott.liang
 * @version 1.1 5/12/2015
 * @since laxcus 1.0
 */
public final class StubSet extends MutexHandler implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 8704387981310205789L;

	/** 数据块编号集合 */
	private TreeSet<Long> array = new TreeSet<Long>();

	/**
	 * 将数据块编号集合写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		// 写入成员数目
		writer.writeInt(array.size());
		// 写入每一个成员
		for (Long stub : array) {
			writer.writeLong(stub.longValue());
		}
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析数据块编号集合
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		// 读成员数目
		int size = reader.readInt();
		// 读每个成员和保存它
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			array.add(stub);
		}
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 根据传入的数据块编号集合实例，生成它的副本
	 * @param that StubSet实例
	 */
	private StubSet(StubSet that) {
		this();
		array.addAll(that.array);
	}

	/**
	 * 构造一个空的数据块编号集合
	 */
	public StubSet() {
		super();
	}

	/**
	 * 构造数据块编号集合，保存数据块编号数组
	 * @param a 数据块编号数组
	 */
	public StubSet(Long[] a) {
		this();
		addAll(a);
	}

	/**
	 * 构造数据块编号集合
	 * @param a 数据块编号集合
	 */
	public StubSet(Collection<Long> a) {
		this();
		addAll(a);
	}

	/**
	 * 从可类化数据读取器中解析数据块编号集合
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public StubSet(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 逻辑"与"操作：保留共有的数据块编号，其它删除。
	 * @param set 数据块编号集合
	 */
	public void AND(Collection<Long> set) {
		array.retainAll(set);
	}

	/**
	 * 逻辑"与"操作：保留共有的数据块编号，其它删除。
	 * @param that StubSet实例
	 */
	public void AND(StubSet that) {
		AND(that.array);
	}

	/**
	 * 逻辑"或"操作：重叠的保留一个，不重叠的也保留
	 * @param set 数据块编号集合
	 */
	public void OR(Collection<Long> set) {
		array.addAll(set);
	}

	/**
	 * 逻辑"或"操作
	 * @param set StubSet实例
	 */
	public void OR(StubSet set) {
		OR(set.array);
	}

	/**
	 * 保存一个数据块编号
	 * @param e 数据块编号
	 * @return 返回真或者假
	 */
	public boolean add(Long e) {
		if (e != null) {
			return array.add(e);
		}
		return false;
	}

	/**
	 * 删除一个数据块编号
	 * @param e 数据块编号
	 * @return 返回真或者假
	 */
	public boolean remove(Long e) {
		return array.remove(e);
	}

	/**
	 * 保存另一个数据块编号集合的全部编号
	 * @param that 另一个数据块编号集合
	 * @return 返回新加入的数据块编号数目
	 */
	public int addAll(StubSet that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that.array);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批数据块编号
	 * @param a 数据块编号数组
	 * @return 已经保存的成员数目
	 */
	public int addAll(Long[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			add(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 保存一批数据块编号
	 * @param that 数据块编号集合
	 * @return 返回新增加的成员数目
	 */
	public int addAll(Collection<Long> that) {
		int size = array.size();
		if (that != null) {
			array.addAll(that);
		}
		return array.size() - size;
	}

	/**
	 * 判断数据块编号存在
	 * @param e 数据块编号
	 * @return 返回真或者假
	 */
	public boolean contains(Long e) {
		if (e != null) {
			return array.contains(e);
		}
		return false;
	}

	/**
	 * 迭代输出
	 * @return 数据块编号迭代
	 */
	public Iterator<Long> iterator() {
		return array.iterator();
	}

	/**
	 * 以非锁定方式和复制数据副本方式，输出全部数据块编号
	 * @return 数据块编号列表
	 */
	public List<Long> list() {
		return new ArrayList<Long>(array);
	}

	/**
	 * 以非锁定方式输入全部数据块编号
	 * @return 数据块编号集合
	 */
	public Set<Long> set() {
		return array;
	}

	/**
	 * 清除全部数据块编号
	 */
	public void clear() {
		array.clear();
	}

	/**
	 * 统计数据块编号的数目
	 * @return 编号数目
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
	 * 以非锁定方式输出全部数据块编号数组
	 * @return 数据块编号数组
	 */
	public Long[] toArray() {
		Long[] a = new Long[array.size()];
		return array.toArray(a);
	}
	
	/**
	 * 检查两个数据块编号集合完全一致
	 * @param that 另一个集合
	 * @return 一致返回真，否则假。
	 */
	public boolean alike(StubSet that) {
		// 1. 参数有效
		boolean success = (that != null);
		// 2. 数目一致
		if (success) {
			success = (size() == that.size());
		}
		// 3. 检查每个表都匹配
		if (success) {
			int count = 0;
			for (Long e : that.array) {
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
	 * 克隆当前数据块编号集合的数据副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new StubSet(this);
	}

	/**
	 * 以锁定方式增加一个数据块编号。不允许空指针或者数据块编号重叠的现象存在。
	 * @param e 数据块编号
	 * @return 增加成功返回“真”，否则“假”。
	 */
	public boolean push(Long e) {
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
	 *以锁定方式删除一个数据块编号。
	 * @param e 数据块编号
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean drop(Long e) {
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
	 * 以锁定方式增加一组数据块编号。每个数据块编号都是唯一的，不允许重叠现象存在。
	 * @param a 数据块编号数组
	 * @return 返回增加的成员数
	 */
	public int push(Long[] a) {
		int size = array.size();
		for (int i = 0; a != null && i < a.length; i++) {
			push(a[i]);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定方式增加一组数据块编号
	 * @param a 运行数据块编号集合
	 * @return 返回增加的成员数
	 */
	public int pushAll(Collection<Long> a) {
		int size = array.size();
		for (Long e : a) {
			push(e);
		}
		return array.size() - size;
	}

	/**
	 * 以锁定的方式删除一组数据块编号。
	 * @param a 数据块编号集合
	 * @return 返回删除的成员数目
	 */
	public int dropAll(Collection<Long> a) {
		int size = array.size();
		for (Long e : a) {
			drop(e);
		}
		return size - array.size();
	}

	/**
	 * 以锁定方式判断数据块编号存在
	 * @param e 数据块编号
	 * @return 存在返回真，否则假
	 */
	public boolean exists(Long e) {
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
	 * 以锁定方式输出全部数据块编号
	 * @return 数据块编号集合
	 */
	public List<Long> show() {
		super.lockMulti();
		try {
			return new ArrayList<Long>(array);
		} finally {
			super.unlockMulti();
		}
	}

	/**
	 * 以锁定方式输出全部数据块编号
	 * @return 数据块编号数组
	 */
	public Long[] array() {
		super.lockMulti();
		try {
			Long[] a = new Long[array.size()];
			return array.toArray(a);
		} finally {
			super.unlockMulti();
		}
	}
}