/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.law;

import java.io.*;

import com.laxcus.access.column.*;
import com.laxcus.access.schema.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 行记录特称。<br>
 * 
 * 由列标记和列值两部分组成，说明一行记录的特别属性。
 * 
 * @author scott.liang
 * @version 1.0 4/10/2018
 * @since laxcus 1.0
 */
public final class RowFeature implements Cloneable, Serializable, Comparable<RowFeature>, Classable {

	private static final long serialVersionUID = -4563026346004119519L;

	/** 指定列 **/
	private Dock dock;
	
	/** 列值 **/
	private Column column;

	/**
	 * 根据传入的行记录特称，生成它的数据副本
	 * @param that RowFeature实例
	 */
	private RowFeature(RowFeature that) {
		this();
		dock = that.dock;
		column = that.column;
	}

	/**
	 * 构造行记录特称，指定操作符
	 */
	public RowFeature() {
		super();
	}

	/**
	 * 构造行记录特称，指定操作符和数据表名
	 * @param dock 列空间
	 * @param column 列值
	 */
	public RowFeature(Dock dock, Column column) {
		this();
		setDock(dock);
		setColumn(column);
	}

	/**
	 * 从可类化数据读取器中解析行记录特称
	 * @param reader 可类化数据读取器
	 */
	public RowFeature(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 设置列空间，不允许空指定针
	 * @param e 列空间
	 */
	public void setDock(Dock e) {
		Laxkit.nullabled(e);
		dock = e;
	}

	/**
	 * 返回列空间
	 * @return 列空间实例
	 */
	public Dock getDock() {
		return dock;
	}
	
	/**
	 * 返回数据库名
	 * @return Fame实例
	 */
	public Fame getFame(){
		return dock.getSchema();
	}
	
	/**
	 * 返回表名
	 * @return Space实例
	 */
	public Space getSpace(){
		return dock.getSpace();
	}

	/**
	 * 设置列值，不允许空指针
	 * @param e 列实例
	 */
	public void setColumn(Column e) {
		Laxkit.nullabled(e);
		column = e;
	}

	/**
	 * 返回列值
	 * @return 列值
	 */
	public Column getColumn() {
		return column;
	}
	
	/**
	 * 返回行记录特称副本
	 * @return 行记录特称
	 */
	public RowFeature duplicate() {
		return new RowFeature(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		return this.duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return dock.hashCode() ^ column.hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.law.rule.RuleItem#toString()
	 */
	@Override
	public String toString() {
		return dock.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RowFeature that) {
		// 空值在前
		if (that == null) {
			return 1;
		}

		// 比较对象
		int ret = Laxkit.compareTo(dock, that.dock);
		// 比较列值，注意！不是列编号！
		if (ret == 0) {
			ret = column.compare(that.column);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(dock);
		writer.writeDefault(column);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		dock = new Dock(reader);
		column = (Column) reader.readDefault();
		return reader.getSeek() - seek;
	}

}