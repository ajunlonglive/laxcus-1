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
 * 扫描数据块单元。
 * 
 * @author scott.liang
 * @version 1.1 5/23/2015
 * @since laxcus 1.0
 */
public final class ScanEntityItem implements Classable, Cloneable, Comparable<ScanEntityItem>, Serializable {
	
	private static final long serialVersionUID = 6155260877463392171L;

	/** 节点地址  **/
	private Node site;
	
	/** 数据表名 **/
	private Space space;
	
	/** 数据块数目 **/
	private long stubs;
	
	/** 数据长度 **/
	private long length;
	
	/**
	 * 构造默认和私有的扫描数据块单元
	 */
	private ScanEntityItem() {
		super();
		stubs = 0;
		length = 0;
	}
	
	/**
	 * 根据传入的实例，生成它的数据副本
	 * @param that ScanEntityItem实例
	 */
	private ScanEntityItem(ScanEntityItem that){
		this();
		site = that.site;
		space = that.space;
		stubs = that.stubs;
		length = that.length;
	}
	
	/**
	 * 构造扫描数据块单元，指定全部参数
	 * @param site 站点地址
	 * @param space 数据表名
	 */
	public ScanEntityItem(Node site, Space space) {
		this();
		setSite(site);
		setSpace(space);
	}

	/**
	 * 从可类化数据读取器中解析扫描数据块单元参数
	 * @param reader 可类化数据读取器
	 * @since laxcus 1.1
	 */
	public ScanEntityItem(ClassReader reader) {
		this();
		resolve(reader);
	}

	 /**
	  * 设置节点地址，不允许空指针
	  * @param e Node实例
	 */
	public void setSite(Node e) {
		Laxkit.nullabled(e);
		site = e;
	}

	/**
	 * 返回节点地址
	 * @return Node实例
	 */
	public Node getSite() {
		return site;
	}

	/**
	 * 设置数据表名，不允许空指针
	 * @param e
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
		return space;
	}

	/**
	 * 设置成功状态
	 * @param b 成功状态
	 */
	public void setLength(long b) {
		length = b;
	}

	/**
	 * 增加容量
	 * @param b
	 */
	public void addLength(long b) {
		length += b;
	}

	/**
	 * 返回数据容量
	 * @return
	 */
	public long getLength() {
		return length;
	}
	
	/**
	 * 设置数据块数目
	 * @param i 数据块数目
	 */
	public void setStubs(long i) {
		stubs = i;
	}
	
	/**
	 * 增加数据块数目
	 * @param i
	 */
	public void addStubs(long i){
		stubs += i;
	}

	/**
	 * 返回数据块数目
	 * @return 数据块数目
	 */
	public long getStubs() {
		return stubs;
	}
	
	/**
	 * 返回当前实例的数据副本
	 * @return ScanEntityItem实例
	 */
	public ScanEntityItem duplicate() {
		return new ScanEntityItem(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != getClass()) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ScanEntityItem) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return site.hashCode();
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
	public int compareTo(ScanEntityItem that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(site, that.site);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(site);
		writer.writeObject(space);
		writer.writeLong(stubs);
		writer.writeLong(length);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		site = new Node(reader);
		space = new Space(reader);
		stubs = reader.readLong();
		length = reader.readLong();
		return reader.getSeek() - seek;
	}

}