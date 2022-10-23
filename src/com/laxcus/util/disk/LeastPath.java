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
 * 资源限制目录 <br><br>
 * 
 * LINUX/WINDOWS设备检测器，定时对这些目录进行检查，判断磁盘空间不足将报警。
 * 
 * @author scott.liang
 * @version 1.0 8/20/2019
 * @since laxcus 1.0
 */
public class LeastPath implements Classable, Serializable, Cloneable, Comparable<LeastPath> {

	private static final long serialVersionUID = -3942619895381199444L;

	/** 磁盘路径 **/
	private String path;

	/** 最小磁盘空间 **/
	private volatile long capacity = -1;

	/** 最小磁盘空间占比 **/
	private volatile double rate = 0.0f;
	
	/**
	 * 构造默认的资源限制目录
	 */
	public LeastPath() {
		super();
		setUnlimit();
	}
	
	/**
	 * 构造默认的资源限制目录
	 */
	public LeastPath(String path) {
		this();
		setPath(path);
	}
	
	/**
	 * 构造资源限制目录，指定参数
	 * @param path 磁盘路径
	 * @param capacity 最小容量
	 */
	public LeastPath(String path, long capacity) {
		this(path);
		setCapacity(capacity);
	}
	
	/**
	 * 构造资源限制目录，指定参数
	 * @param path 磁盘路径
	 * @param rate 最小容量
	 */
	public LeastPath(String path, double rate) {
		this(path);
		setRate(rate);
	}

	/**
	 * 从可类化数据读取器中解析资源限制目录
	 * @param reader 可类化数据读取器
	 */
	public LeastPath(ClassReader reader) {
		this();
		resolve(reader);
	}
	
	/**
	 * 设置磁盘路径
	 * @param e 磁盘路径
	 */
	public void setPath(String e) {
		Laxkit.nullabled(e);
		path = e;
	}

	/**
	 * 返回磁盘路径
	 * @return 磁盘路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 返回目录
	 * @return File实例
	 */
	public File getDirectory() {
		return new File(path);
	}

	/**
	 * 设置为无限制
	 */
	public void setUnlimit() {
		capacity = -1;
		rate = 0.0f;
	}

	/**
	 * 设置最小磁盘空间，小于1是不限制磁盘空间。
	 * @param len 空间尺寸
	 */
	public void setCapacity(long len) {
		capacity = len;
	}

	/**
	 * 返回最小磁盘空间
	 * @return 最小磁盘空间数
	 */
	public long getCapacity() {
		return capacity;
	}
	
	/**
	 * 最低磁盘空间占比
	 * @param b 双浮点数
	 */
	public void setRate(double b) {
		rate = b;
	}

	/**
	 * 返回最低磁盘空间占比
	 * @return 最低磁盘空间占比
	 */
	public double getRate() {
		return rate;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s - %d - %.3f", path, capacity, rate);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != LeastPath.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((LeastPath) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return path.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LeastPath that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(path, that.path, false);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeString(path);
		writer.writeLong(capacity);
		writer.writeDouble(rate);
		return writer.size() - size;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		path = reader.readString();
		capacity = reader.readLong();
		rate = reader.readDouble();
		return reader.getSeek() - seek;
	}

}