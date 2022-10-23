/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.scan;

import java.io.*;

import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 数据卷扫描单元 <br>
 * 包括表名、行数、有效行数三个部分。是对多个节点的数据表单元的合并结果。
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public final class ScanVolumeItem implements Classable, Serializable, Cloneable, Comparable<ScanVolumeItem> {

	private static final long serialVersionUID = -3325008346879145860L;

	/** 数据表名 **/
	private Space space;

	/** 数据块总数 **/
	private int stubs;
	
	/** 全部行统计 **/
	private long rows;

	/** 有效行统计 **/
	private long availableRows;

	/**
	 * 生成传入实例的数据副本
	 * @param that 数据卷扫描单元实例
	 */
	private ScanVolumeItem(ScanVolumeItem that) {
		super();
		space = that.space;
		stubs = that.stubs;
		rows = that.rows;
		availableRows = that.availableRows;
	}

	/**
	 * 构造数据卷扫描单元实例
	 */
	public ScanVolumeItem() {
		super();
		rows = availableRows = 0L;
	}

	/**
	 * 构造数据卷扫描单元，指定数据表名
	 * @param space 数据表名
	 */
	public ScanVolumeItem(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 从可类数据读取器中解析数据卷扫描单元参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanVolumeItem(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 累加数据卷扫描单元
	 * @param e 数据卷扫描单元实例
	 * @return 成功返回真，否则假
	 */
	public boolean cumulate(ScanVolumeItem e) {
		// 必须表名一致
		if (Laxkit.compareTo(space, e.space) != 0) {
			return false;
		}
		stubs += e.stubs;
		rows += e.rows;
		availableRows += e.availableRows;
		return true;
	}

	/**
	 * 设置数据表名
	 * @param e 表名
	 */
	public void setSpace(Space e) {
		Laxkit.nullabled(e);

		space = e;
	}

	/**
	 * 返回数据表名
	 * @return 表名
	 */
	public Space getSpace() {
		return space;
	}

	/**
	 * 设置数据块数目
	 * @param i 数据块数目
	 */
	public void setStubs(int i) {
		stubs = i;
	}

	/**
	 * 返回数据块数目
	 * @return 数据块数目
	 */
	public int getStubs() {
		return stubs;
	}

	/**
	 * 设置总行数
	 * @param i 行数
	 */
	public void setRows(long i) {
		rows = i;
	}

	/**
	 * 返回总行数
	 * @return 总行数，long类型
	 */
	public long getRows() {
		return rows;
	}

	/**
	 * 设置有效行数
	 * @param i 有效行数
	 */
	public void setAvailableRows(long i) {
		availableRows = i;
	}

	/**
	 * 返回有效行数
	 * @return  有效行数
	 */
	public long getAvailableRows() {
		return availableRows;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ScanVolumeItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ScanVolumeItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return space.hashCode();
	}

	/**
	 * 生成当前对象的数据副本
	 * @return 数据卷扫描单元实例
	 */
	public ScanVolumeItem duplicate() {
		return new ScanVolumeItem(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ScanVolumeItem that) {
		if (that == null) {
			return 1;
		}
		// 比较
		return Laxkit.compareTo(space, that.space);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeObject(space);
		writer.writeInt(stubs);
		writer.writeLong(rows);
		writer.writeLong(availableRows);
		// 返回长度
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		space = new Space(reader);
		stubs = reader.readInt();
		rows = reader.readLong();
		availableRows = reader.readLong();
		// 返回读取尺寸
		return reader.getSeek() - seek;
	}

}