/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.index;

import java.io.*;
import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块成员集合。<br><br>
 * 
 * 数据块成员集合表述一个DATA站点下，某一个表空间下全部数据块的元数据。它由两个参数组成：<br>
 * 1. 数据表名 <br>
 * 2. 数据块成员集合 <br>
 * 
 * @author scott.liang
 * @version 1.2 8/12/2015
 * @since laxcus 1.0
 */
public final class StubArea implements Classable, Cloneable, Serializable, Comparable<StubArea> {

	private static final long serialVersionUID = 4492012479867889544L;

	/** 数据表名 **/
	private Space space;
	
	/** 剩余空间尺寸 **/
	private long left;

	/** 数据块成员 **/
	private TreeSet<StubItem> array = new TreeSet<StubItem>();

	/**
	 * 根据传入实例，生成它的数据副本
	 * @param that StubArea实例
	 */
	private StubArea(StubArea that) {
		super();
		space = that.space;
		left = that.left;
		array.addAll(that.array);
	}

	/**
	 * 构造默认的数据块成员集合
	 */
	private StubArea() {
		super();
	}

	/**
	 * 构造数据块成员集合，指定全部参数
	 * @param space  数据表名
	 */
	public StubArea(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析数据块成员集合
	 * @param reader  可类化数据读取器
	 * @since 1.2
	 */
	public StubArea(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据表名，不允许空值
	 * @param e  Space实例
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return Space实例
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置剩余空间尺寸
	 * @param i 剩余空间尺寸
	 */
	public void setLeft(long i) {
		left = i;
	}

	/**
	 * 返回剩余空间尺寸
	 * @return 剩余空间尺寸
	 */
	public long getLeft() {
		return left;
	}
	
	/**
	 * 保存一个数据块元数据
	 * @param e 数据块元数据实例
	 * @return 成功返回真，否则假
	 */
	public boolean add(StubItem e) {
		Laxkit.nullabled(e);

		return array.add(e);
	}

	/**
	 * 保存一批数据块元数据
	 * @param a 数据块元数据列表
	 * @return 返回新增成员数目
	 */
	public int addAll(Collection<StubItem> a) {
		int size = array.size();
		for (StubItem e : a) {
			add(e);
		}
		return array.size() - size;
	}

	/**
	 * 输出全部数据块元数据
	 * @return  List<StubItem>
	 */
	public List<StubItem> list() {
		return new ArrayList<StubItem>(array);
	}

	/**
	 * 返回数据块元数据数目
	 * @return  数据块元数据的整型值
	 */
	public int size() {
		return array.size();
	}

	/**
	 * 判断是空集
	 * @return  返回真或者假
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 复制数据块成员集合浅层数据副本
	 * @return StubArea实例
	 */
	public StubArea duplicate() {
		return new StubArea(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != StubArea.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((StubArea) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return space.hashCode() ;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s/%d/%d", space, left, array.size());
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
	public int compareTo(StubArea that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(space, that.space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 数据表名
		writer.writeObject(space);
		// 剩余空间尺寸
		writer.writeLong(left);
		// 数据块成员
		writer.writeInt(array.size());
		for (StubItem item : array) {
			writer.writeObject(item);
		}
		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 数据表名
		space = new Space(reader);
		// 剩余空间尺寸
		left = reader.readLong();
		// 数据块成员
		int size = reader.readInt();
		for (int i = 0; i < size; i++) {
			StubItem item = new StubItem(reader);
			array.add(item);
		}
		return reader.getSeek() - seek;
	}

}