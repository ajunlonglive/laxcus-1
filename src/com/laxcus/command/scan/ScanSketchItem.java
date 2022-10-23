/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.command.scan;

import java.io.*;

import com.laxcus.site.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 表数据容量检测单元。<br><br>
 * 
 * 数据容量内容包括：数据块数目、数据块文件尺寸、全部行数、有效行数。<br>
 * 通过数据容量分析，用户决定使用MODULATE、REGULATE来优化数据。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 9/25/2015
 * @since laxcus 1.0
 */
public abstract class ScanSketchItem implements Classable, Cloneable, Serializable {
	
	private static final long serialVersionUID = 3349887947140697116L;

	/** 站点级别 **/
	private byte rank;
	
	/** 数据块统计 **/
	private long stubs;
	
	/** 数据块总长度 **/
	private long length;
	
	/** 总行数 **/
	private long rows;
	
	/** 有效行数 **/
	private long avaliableRows;

	/**
	 * 构造默认和私有的表数据容量检测单元
	 */
	protected ScanSketchItem() {
		super();
	}

	/**
	 * 构造表数据容量检测单元，指定站点级别
	 * @param rank 站点级别
	 */
	protected ScanSketchItem(byte rank) {
		this();
		setRank(rank);
	}

	/**
	 * 生成表数据容量检测单元数据副本
	 * @param that CapacityItem实例
	 */
	protected ScanSketchItem(ScanSketchItem that) {
		this();
		rank = that.rank;
		stubs = that.stubs;
		length = that.length;
		rows = that.rows;
		avaliableRows = that.avaliableRows;
	}
	
	/**
	 * 增加单元
	 * @param that CapacityItem实例
	 */
	public void add(ScanSketchItem that) {
		if (rank != that.rank) {
			throw new IllegalValueException("cannot be match:%d - %d", rank, that.rank);
		}
		stubs += that.stubs;
		length += that.length;
		rows += that.rows;
		avaliableRows += that.avaliableRows;
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
	public long getStubs() {
		return stubs;
	}

	/**
	 * 设置数据块总长度
	 * @param i
	 */
	public void setLength(long i) {
		length = i;
	}

	/**
	 * 返回数据块总长度
	 * @return 数据块总长度
	 */
	public long getLength() {
		return length;
	}

	/**
	 * 设置总行数
	 * @param i 总行数
	 */
	public void setRows(long i) {
		rows = i;
	}

	/**
	 * 返回总行数
	 * @return 总行数
	 */
	public long getRows() {
		return rows;
	}

	/**
	 * 设置总有效行数
	 * @param i 总有效行数
	 */
	public void setAvaliableRows(long i) {
		avaliableRows = i;
	}

	/**
	 * 返回总有效行数
	 * @return 总有效行数
	 */
	public long getAvaliableRows() {
		return avaliableRows;
	}

	/**
	 * 设置站点级别
	 * @param who 站点级别
	 */
	public void setRank(byte who) {
		if (!RankTag.isRank(who)) {
			throw new IllegalValueException("illegal rank %d", who);
		}
		rank = who;
	}

	/**
	 * 返回站点级别
	 * @return 站点级别
	 */
	public byte getRank() {
		return rank;
	}

	/**
	 * 判断是主站点
	 * @return 返回真或者假
	 */
	public boolean isMaster() {
		return RankTag.isMaster(rank);
	}

	/**
	 * 判断是从站点
	 * @return 返回真或者假
	 */
	public boolean isSlave() {
		return RankTag.isSlave(rank);
	}

	/**
	 * CapacityItem子类实例生成自己的数据副本
	 * @return CapacityItem子类实例
	 */
	public abstract ScanSketchItem duplicate();

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
		int size = writer.size();
		writer.write(rank);
		writer.writeLong(stubs);
		writer.writeLong(length);
		writer.writeLong(rows);
		writer.writeLong(avaliableRows);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		rank = reader.read();
		stubs = reader.readLong();
		length = reader.readLong();
		rows = reader.readLong();
		avaliableRows = reader.readLong();
		return reader.getSeek() - seek;
	}

}
