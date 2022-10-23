/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块记录<br>
 * 
 * 数据块记录描述一段区域内的数据构成，由一个数据表名和任意数量的数据块编号组成。
 * 
 * @author scott.liang
 * @version 1.1 11/12/2015
 * @since laxcus 1.0
 */
public abstract class StubNote implements Classable, Serializable, Cloneable {

	private static final long serialVersionUID = 6960434548191301581L;

	/** 数据表名 **/
	private Space space;

	/** 数据块编号集合 **/
	private TreeSet<java.lang.Long> array = new TreeSet<java.lang.Long>();

	/**
	 * 构造默认的数据块记录
	 */
	protected StubNote() {
		super();
	}

	/**
	 * 根据传入的数据块记录，生成它的数据副本
	 * @param that 数据块记录实例
	 */
	protected StubNote(StubNote that) {
		this();
		setSpace(that.space);
		array.addAll(that.array);
	}

	/**
	 * 设置数据表名，不允许空值
	 * @param e 数据表名
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return 数据表名
	 */
	public Space getSpace() {
		return this.space;
	}

	/**
	 * 保存一个数据块编号 
	 * @param stub 数据块编号
	 * @return 保存成功返回“真”，否则“假”。
	 */
	public boolean add(Long stub) {
		return array.add(stub);
	}

	/**
	 * 保存一批数据块编号
	 * @param that 数据块编号集合
	 * @return 返回新增加的数据块编号数目
	 */
	public int addAll(Collection<Long> that) {
		int size = array.size();
		array.addAll(that);
		return array.size() - size;
	}

	/**
	 * 删除数据块编号
	 * @param stub 数据块编号
	 * @return 删除成功返回“真”，否则“假”。
	 */
	public boolean remove(Long stub) {
		return array.remove(stub);
	}

	/**
	 * 判断数据块编号存在
	 * @param stub
	 * @return 存在返回“真”，否则“假”。
	 */
	public boolean contains(Long stub) {
		return array.contains(stub);
	}

	/**
	 * 返回数据块编号列表
	 * @return 数据块编号列表
	 */
	public List<Long> list() {
		return new ArrayList<Long>(array);
	}

	/**
	 * 返回数据块编号数目
	 * @return 数据块编号数目的整型值
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断数据块编号集合是空
	 * @return 返回真或者假
	 */
	public boolean isEmpty() {
		return array.isEmpty();
	}

	/**
	 * 以长整形数组输出数据块编号
	 * @return long数组
	 */
	public long[] array() {
		List<Long> list = list();
		long[] a = new long[list.size()];
		for (int i = 0; i < a.length; i++) {
			a[i] = list.get(i).longValue();
		}
		return a;
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
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 写数据表名
		writer.writeObject(space);
		// 写数据块
		writer.writeInt(array.size());
		for (java.lang.Long stub : array) {
			writer.writeLong(stub.longValue());
		}
		// 写入子集数据
		buildSuffix(writer);
		// 返回写入的字节长度
		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 数据表名
		space = new Space(reader);
		// 数据块编号集合
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			long stub = reader.readLong();
			array.add(new java.lang.Long(stub));
		}
		// 读取子集数据
		resolveSuffix(reader);
		// 返回读取的字节长度
		return reader.getSeek() - scale;
	}

	/**
	 * 子类实例产生一个自己的数据副本
	 * @return StubNote子类实例副本
	 */
	public abstract StubNote duplicate();

	/**
	 * 将索引参数子类信息写入可类化数据存储器
	 * @param writer 可类化数据存储器
	 * @since 1.1
	 */
	protected abstract void buildSuffix(ClassWriter writer);

	/**
	 * 从可类化数据读取器中解析索引参数子类信息
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	protected abstract void resolveSuffix(ClassReader reader);
}
