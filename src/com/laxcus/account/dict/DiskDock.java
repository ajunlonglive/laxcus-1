/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.dict;

import java.io.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;

/**
 * 账号磁盘坐标 <br>
 * 
 * 标记一个账号在账号服务器磁盘的位置，包括三个参数：文件编号、数据在磁盘文件的开始位置、数据长度。
 * 一个账号磁盘坐标占12个字节空间。
 * 
 * @author scott.liang
 * @version 1.0 6/24/2018
 * @since laxcus 1.0
 */
public final class DiskDock implements Classable, Serializable, Cloneable, Comparable<DiskDock> {

	private static final long serialVersionUID = 9035864448657792688L;

	/** 文件编号 **/
	private int no;
	
	/** 文件下标位置 **/
	private int offset;
	
	/** 数据长度 **/
	private int length;
	
	/**
	 * 固定空间容量，12个字节
	 * @return 12字节
	 */
	public static int capacity() {
		return 12;
	}

	/**
	 * 将账号磁盘坐标写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 文件编号
		writer.writeInt(no);	
		// 文件下标位置
		writer.writeInt(offset);
		// 数据总长度
		writer.writeInt(length);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析账号磁盘坐标
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 文件编号
		no = reader.readInt();
		// 文件下标位置
		offset = reader.readInt();
		// 数据总长度
		length = reader.readInt();
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个私有的账号磁盘坐标
	 */
	public DiskDock() {
		super();
	}

	/**
	 * 根据传入的账号磁盘坐标，生成它的数据副本
	 * @param that DiskDock实例
	 */
	private DiskDock(DiskDock that) {
		this();
		no = that.no;
		offset = that.offset;
		length = that.length;
	}

	/**
	 * 构造账号磁盘坐标，指定文件编号、数据下标、数据长度
	 * @param no 文件编号
	 * @param offset 文件下标
	 * @param length 数据长度
	 */
	public DiskDock(int no, int offset, int length) {
		this();
		setNo(no);
		setOffset(offset);
		setLength(length);
	}

	/**
	 * 从可类化数据读取器中解析账号磁盘坐标
	 * @param reader 可类化数据读取器
	 */
	public DiskDock(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从传入的字节数组中解析账号磁盘坐标参数
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 */
	public DiskDock(byte[] b, int off, int len) {
		this();
		resolve(b, off, len);
	}

	/**
	 * 设置文件编号
	 * @param e int实例
	 */
	public void setNo(int e) {
		no = e;
	}

	/**
	 * 返回文件编号
	 * @return int实例
	 */
	public int getNo() {
		return no;
	}

	/**
	 * 设置文件下标位置
	 * @param e int实例
	 */
	public void setOffset(int e) {
		offset = e;
	}

	/**
	 * 返回文件下标位置
	 * @return int实例
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * 设置账号域总长度
	 * @param e int实例
	 */
	public void setLength(int e) {
		length = e;
	}

	/**
	 * 返回账号域总长度
	 * @return int实例
	 */
	public int getLength() {
		return length;
	}

	/**
	 * 建立一个当前账号磁盘坐标的数据副本
	 * @return DiskDock实例
	 */
	public DiskDock duplicate() {
		return new DiskDock(this);
	}

	/**
	 * 比较两个账号磁盘坐标一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != DiskDock.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((DiskDock) that) == 0;
	}

	/**
	 * 返回账号磁盘坐标的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return  no ^ offset ^ length;
	}

	/**
	 * 返回账号磁盘坐标的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%d # %d # %d", no, offset, length);
	}

	/**
	 * 根据当前账号磁盘坐标，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DiskDock that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		// 比较文件编号、数据在磁盘文件下标、数据长度
		int ret = Laxkit.compareTo(no, that.no);
		if (ret == 0) {
			ret = Laxkit.compareTo(offset, that.offset);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(length, that.length);
		}
		return ret;
	}

	/**
	 * 账号磁盘坐标生成数据流输出
	 * @return 返回字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从数据流中解析账号磁盘坐标，返回解析的长度
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 * @return 返回解析的长度
	 */
	public int resolve(byte[] b, int off, int len) {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}
}