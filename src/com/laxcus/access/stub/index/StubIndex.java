/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.index;

import java.io.Serializable;

import com.laxcus.access.stub.chart.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.range.*;

/**
 * 数据块索引 <br><br>
 * 
 * 数据块索引是基于StubElement，子类型包括：SHORT、INT、LONG、FLOAT、DOUBLE，这5种数据类型。
 * 
 * @author scott.liang
 * @version 1.0 3/23/2009
 * @since laxcus 1.0
 */
public abstract class StubIndex implements Serializable, Cloneable, Classable, Comparable<StubIndex> {

	private static final long serialVersionUID = -7057023952002490788L;

	/** 索引类型，见com.laxcus.access.IndexType定义 */
	private byte family;

	/** 列编号 */
	private short columnId;

	/**
	 * 将数据块索引写入可类化写入器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int scale = writer.size();
		// 索引类型
		writer.write(family);
		// 索引列编号
		writer.writeShort(columnId);
		// 调用子类生成各自的信息
		buildSuffix(writer);
		// 返回写入的字节数
		return writer.size() - scale;
	}
	
	/**
	 * 从可类化读取器中解析数据块索引属性
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int scale = reader.getSeek();
		// 索引类型
		family = reader.read();
		// 列编号
		columnId = reader.readShort();
		// 调用子类解析参数
		resolveSuffix(reader);
		// 返回读取的字节数
		return reader.getSeek() - scale;
	}

	/**
	 * 构造一个默认的数据块索引
	 */
	protected StubIndex() {
		super();
		// 全部无效
		family = 0;
		columnId = 0;
	}

	/**
	 * 根据传入的数据块索引，生成它的副本
	 * @param that StubIndex实例
	 */
	protected StubIndex(StubIndex that) {
		super();
		family = that.family;
		columnId = that.columnId;
	}

	/**
	 * 构造数据块索引，并且指定它的索引数据类型
	 * @param family 索引类型
	 */
	protected StubIndex(byte family) {
		this();
		setFamily(family);
	}

	/**
	 * 构造数据块索引，并且指定它的索引数据类型、表的列编号
	 * @param family 索引类型
	 * @param columnId 列编号
	 */
	protected StubIndex(byte family, short columnId) {
		this();
		setFamily(family);
		setColumnId(columnId);
	}

	/**
	 * 设置列编号
	 * @param id 列编号
	 */
	public void setColumnId(short id) {
		columnId = id;
	}

	/**
	 * 返回列编号
	 * @return 列编号
	 */
	public short getColumnId() {
		return columnId;
	}

	/**
	 * 设置索引类型
	 * @param who
	 */
	public void setFamily(byte who) {
		switch (who) {
		case IndexType.SHORT_INDEX:
		case IndexType.INTEGER_INDEX:
		case IndexType.LONG_INDEX:
		case IndexType.FLOAT_INDEX:
		case IndexType.DOUBLE_INDEX:
			family = who;
			break;
		default:
			throw new IllegalValueException("illegal index: %d", who);
		}
	}

	/**
	 * 返回索引类型
	 * @return 索引类型的字节描述
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 判断是否SHORT索引类型
	 * @return 返回真或者假
	 */
	public boolean isShort() {
		return IndexType.isShortIndex(family); 
	}

	/**
	 * 判断是否INT索引类型
	 * @return 返回真或者假
	 */
	public boolean isInteger() {
		return IndexType.isIntegerIndex(family); 
	}

	/**
	 * 判断是否LONG索引类型
	 * @return 返回真或者假
	 */
	public boolean isLong() {
		return IndexType.isLongIndex(family); 
	}

	/**
	 * 判断是否FLOAT索引类型
	 * @return 返回真或者假
	 */
	public boolean isFloat() {
		return IndexType.isFloatIndex(family); 
	}

	/**
	 * 判断是否DOUBLE索引类型
	 * @return 返回真或者假
	 */
	public boolean isDouble() {
		return IndexType.isDoubleIndex(family); 
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || getClass() != that.getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}

		return compareTo((StubIndex) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return columnId;
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
	public int compareTo(StubIndex that) {
		// 空对象排在前面，有效对象排在后面
		if (that == null) {
			return 1;
		}

		// 在列匹配的情况下比较数据块标识号
		return Laxkit.compareTo(columnId, that.columnId);
	}
	
	/**
	 * 返回当前数据范围
	 * @return  Range子类实例
	 */
	public abstract Range getRange();
	
	/**
	 * 建立一个对应的列索引图表
	 * @return StubChart子类实例
	 */
	public abstract StubChart createStubChart();

	/**
	 * 根据实际数据块索引实例，构造它的副本
	 * @return StubIndex子类实例
	 */
	public abstract StubIndex duplicate();
	
	/**
	 * 向可类化存储器中填充属于自己的参数，由子类实现。
	 * @param writer 可类化写入器
	 */
	protected abstract void buildSuffix(ClassWriter writer);
	
	/**
	 * 从数组读取器中提取属于自己的参数，由子类实现
	 * @param reader 可类化读取器
	 */
	protected abstract void resolveSuffix(ClassReader reader); 
}