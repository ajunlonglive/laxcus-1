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
import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * “SCAN TABLE”命令处理单元。<br>
 * 以一个DATA主机地址和一个数据表名为单位。
 * 
 * @author scott.liang
 * @version 1.1 8/23/2015
 * @since laxcus 1.0
 */
public final class ScanTableItem implements Classable, Serializable, Cloneable, Comparable<ScanTableItem> {

	private static final long serialVersionUID = 8687480744117965032L;

	/** DATA主机地址 **/
	private Node site;

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
	 * @param that ScanTableItem实例
	 */
	private ScanTableItem(ScanTableItem that) {
		super();
		site = that.site;
		space = that.space;
		stubs = that.stubs;
		rows = that.rows;
		availableRows = that.availableRows;
	}

	/**
	 * 构造ScanTableItem实例
	 */
	public ScanTableItem() {
		super();
		stubs = 0;
		rows = availableRows = 0L;
	}

	/**
	 * 构造ScanTableItem，指定数据表名
	 * @param space 数据表名
	 */
	public ScanTableItem(Space space) {
		this();
		setSpace(space);
	}

	/**
	 * 构造ScanTableItem，指定节点地址和数据表名
	 * @param local 节点地址
	 * @param space 数据表名
	 */
	public ScanTableItem(Node local, Space space) {
		this();
		setSite(local);
		setSpace(space);
	}

	/**
	 * 从可类数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public ScanTableItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置DATA站点地址
	 * @param e DATA站点地址
	 */
	public void setSite(Node e) {
		site = e;
	}

	/**
	 * 返回DATA站点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 判断是主节点
	 * @return 返回真或者假
	 */
	public boolean isMaster() {
		return site.isMaster();
	}

	/**
	 * 判断是从节点
	 * @return 返回真或者假
	 */
	public boolean isSlave() {
		return site.isSlave();
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
	
	/**
	 * 生成一个数据卷扫描单元
	 * @return 数据卷扫描单元
	 */
	public ScanVolumeItem createVolume() {
		ScanVolumeItem e = new ScanVolumeItem(space);
		e.setStubs(stubs);
		e.setRows(rows);
		e.setAvailableRows(availableRows);
		return e;
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
		if (that == null || that.getClass() != ScanTableItem.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ScanTableItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if(site != null) {
			return site.hashCode() ^ space.hashCode();
		}
		return space.hashCode();
	}

	/**
	 * 生成当前对象的数据副本
	 * @return ScanTableItem实例
	 */
	public ScanTableItem duplicate() {
		return new ScanTableItem(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ScanTableItem that) {
		if (that == null) {
			return 1;
		}

		// 比较主机地址
		int ret = Laxkit.compareTo(site, that.site);
		// 比较数据表名
		if (ret == 0) {
			ret = Laxkit.compareTo(space, that.space);
		}
		// 返回结果
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int size = writer.size();
		writer.writeInstance(site);
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
		site = reader.readInstance(Node.class);
		space = new Space(reader);
		stubs = reader.readInt();
		rows = reader.readLong();
		availableRows = reader.readLong();
		// 返回读取尺寸
		return reader.getSeek() - seek;
	}

}