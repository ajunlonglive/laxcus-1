/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.disk;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 磁盘目录标记。
 * 
 * 记录磁盘目录名称，全部、剩余空间尺寸。如果是LINUX目录，有文件系统编号。
 * 
 * @author scott.liang
 * @version 1.0 2/10/2019
 * @since laxcus 1.0
 */
public class PathTab implements Classable, Serializable, Cloneable, Comparable<PathTab> {

	private static final long serialVersionUID = -355145708660328198L;
	
	/** 文件系统编号 **/
	private long sid;

	/** 盘符 **/
	private String path;

	/** 全部尺寸 **/
	private long total;

	/** 磁盘没有使用的空间尺寸 **/
	private long free;


	/**
	 * 构造默认的磁盘目录标记
	 */
	public PathTab() {
		super();
		sid = -1L;
	}
	
	/**
	 * 构造磁盘目录标记，指定参数
	 * @param path 磁盘路径
	 */
	public PathTab(String path) {
		this();
		setPath(path);
	}
	
	/**
	 * 从可类化数据读取器中解析磁盘目录标记
	 * @param reader 可类化数据读取器
	 */
	public PathTab(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置文件系统编号，只限LINUX系统。
	 * @param id 文件系统编号
	 */
	public void setSID(long id) {
		sid = id;
	}

	/**
	 * 返回文件系统编号，只限LINUX系统。
	 * @return 文件系统编号
	 */
	public long getSID() {
		return sid;
	}

	/**
	 * 设置盘符
	 * @param e 盘符
	 */
	public void setPath(String e) {
		Laxkit.nullabled(e);
		path = e;
	}

	/**
	 * 返回盘符
	 * @return 盘符
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * 设置磁盘空间总尺寸
	 * @param e 磁盘空间总尺寸
	 */
	public void setTotal(long e) {
		total = e;
	}

	/**
	 * 返回磁盘空间总尺寸
	 * @return 磁盘空间总尺寸
	 */
	public long getTotal(){
		return total;
	}
	
	/**
	 * 设置磁盘没有使用的空间尺寸
	 * @param e 磁盘没有使用的空间尺寸
	 */
	public void setFree(long e) {
		free = e;
	}

	/**
	 * 返回磁盘没有使用的空间尺寸
	 * @return 磁盘没有使用的空间尺寸
	 */
	public long getFree() {
		return free;
	}

	/**
	 * 返回磁盘空间已经占用尺寸
	 * @return 磁盘空间已经占用尺寸
	 */
	public long getUsed() {
		return total - free;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (sid != -1L) {
			return String.format("%s (%x %s %s %s)", path, sid,
					ConfigParser.splitCapacity(total, 2),
					ConfigParser.splitCapacity(total - free, 2),
					ConfigParser.splitCapacity(free, 2));
		} else {
			return String.format("%s (%s %s %s)", path,
					ConfigParser.splitCapacity(total, 2),
					ConfigParser.splitCapacity(total - free, 2),
					ConfigParser.splitCapacity(free, 2));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != PathTab.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((PathTab) that) == 0;
	}

	/*
	 * 
	 */
	@Override
	public int hashCode() {
		return path.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PathTab that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(path, that.path);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeLong(sid);
		writer.writeString(path);
		writer.writeLong(total);
		writer.writeLong(free);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		sid = reader.readLong();
		path = reader.readString();
		total = reader.readLong();
		free = reader.readLong();
		return reader.getSeek() - seek;
	}

}