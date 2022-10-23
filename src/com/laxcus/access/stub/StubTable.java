/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据块索引表。<br><br>
 * 
 * 数据块索引表是DATA站点下，对一个表下属的全部数据块的记录。<br>
 * 
 * 数据块索引表由一个数据表名和任意数量的数据块编号组成。在此基础上，分离出需要的数据，用于分布计算。<br>
 * 
 * @author scott.liang 
 * @version 1.1 11/12/2015
 * @since laxcus 1.0
 */
public final class StubTable extends StubNote implements Comparable<StubTable> {

	private static final long serialVersionUID = 5166221513613035407L;

	/** 数据块目录下的剩余磁盘空间尺寸 **/
	private long left;

	/** 实体数据占用的空间尺寸 **/
	private long available;

	/**
	 * 根据传入参数，生成数据块索引表的副本
	 * @param that
	 */
	private StubTable(StubTable that) {
		super(that);
		left = that.left;
		available = that.available;
	}

	/**
	 * 建立一个默认的数据块索引表
	 */
	public StubTable() {
		super();
		available = left = 0L;
	}

	/**
	 * 建立数据块索引表，指定数据表名
	 * @param space 表名
	 */
	public StubTable(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 构造数据块索引表，指定数据表名和数据块总尺寸
	 * @param space 表名
	 * @param available 已经使用的磁盘空间
	 * @param left 剩余磁盘空间
	 */
	public StubTable(Space space, long available, long left) {
		this(space);
		setAvailable(available);
		setLeft(left);
	}

	/**
	 * 从可类化数据读取器中解析数据块索引表
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public StubTable(ClassReader reader) {
		this();
		resolve(reader);
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
	 * 设置实体数据的磁盘空间
	 * @param i 磁盘空间
	 */
	public void setAvailable(long i) {
		available = i;
	}

	/**
	 * 返回实体数据的磁盘空间
	 * @return 磁盘空间
	 */
	public long getAvailable() {
		return available;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.StubNote#duplicate()
	 */
	@Override
	public StubTable duplicate() {
		return new StubTable(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StubTable that) {
		if(that == null) {
			return 1;
		}
		return Laxkit.compareTo(getSpace(), that.getSpace());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != StubTable.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((StubTable) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getSpace().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s#%d/%d", getSpace(), available, left);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.StubNote#buildSuffix(com.laxcus.util.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(left);
		writer.writeLong(available);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.stub.StubNote#resolveSuffix(com.laxcus.util.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		left = reader.readLong();
		available = reader.readLong();
	}

}