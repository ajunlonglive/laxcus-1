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
 * 账号坐标 <br>
 * 
 * 标记一个账号在账号服务器磁盘的位置，包括四个参数：用户签名、磁盘文件编号、数据在磁盘文件的下标位置、数据在磁盘文件中的长度。
 * 一个账号坐标44个字节。
 * 
 * @author scott.liang
 * @version 1.0 6/24/2018
 * @since laxcus 1.0
 */
public final class AccountDock implements Classable, Serializable, Cloneable, Comparable<AccountDock> {

	private static final long serialVersionUID = 9035864448657792688L;

	/** 用户签名 **/
	private Siger siger;

	/** 磁盘坐标 **/
	private DiskDock dock;

	/**
	 * 账号坐标空间尺寸，共44个字节
	 * @return 账号坐标空间尺寸
	 */
	public static int capaicty() {
		return 44;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// 用户签名
		writer.writeObject(siger);
		// 磁盘坐标
		writer.writeObject(dock);
		// 返回写入的字节长度
		return writer.size() - size;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		// 用户签名
		siger = new Siger(reader);
		// 磁盘坐标
		dock = new DiskDock(reader);
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 构造一个账号坐标
	 */
	public AccountDock() {
		super();
	}

	/**
	 * 根据传入的账号坐标，生成它的数据副本
	 * @param that 账号坐标实例
	 */
	private AccountDock(AccountDock that) {
		this();
		siger = that.siger.duplicate();
		dock = that.dock.duplicate();
	}

	/**
	 * 构造账号坐标，指定用户签名、磁盘坐标
	 * @param siger 用户签名
	 * @param dock 磁盘坐标
	 */
	public AccountDock(Siger siger, DiskDock dock) {
		this();
		setSiger(siger);
		setDock(dock);
	}

	/**
	 * 从可类化数据读取器中解析账号坐标
	 * @param reader 可类化数据读取器
	 */
	public AccountDock(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从传入的字节数组中解析账号坐标参数
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 有效长度
	 */
	public AccountDock(byte[] b, int off, int len) {
		this();
		resolve(b, off, len);
	}

	/**
	 * 设置用户签名
	 * @param e 用户签名
	 */
	public void setSiger(Siger e) {
		Laxkit.nullabled(e);
		siger = e.duplicate();
	}

	/**
	 * 返回用户签名
	 * @return 用户签名
	 */
	public Siger getSiger() {
		return siger;
	}

	/**
	 * 设置磁盘坐标
	 * @param e DiskDock实例
	 */
	public void setDock(DiskDock e) {
		Laxkit.nullabled(e);
		dock = e;
	}

	/**
	 * 返回磁盘坐标
	 * @return DiskDock实例
	 */
	public DiskDock getDock() {
		return dock;
	}

	/**
	 * 返回文件编号
	 * @return int实例
	 */
	public int getNo() {
		return dock.getNo();
	}

	/**
	 * 返回文件下标位置
	 * @return int实例
	 */
	public int getOffset() {
		return dock.getOffset();
	}

	/**
	 * 返回账号域总长度
	 * @return int实例
	 */
	public int getLength() {
		return dock.getLength();
	}

	/**
	 * 建立一个当前账号坐标的数据副本
	 * @return 账号坐标实例
	 */
	public AccountDock duplicate() {
		return new AccountDock(this);
	}

	/**
	 * 比较两个账号坐标一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != AccountDock.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((AccountDock) that) == 0;
	}

	/**
	 * 返回账号坐标的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return siger.hashCode() ^ dock.hashCode();
	}

	/**
	 * 返回账号坐标的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s %s", siger, dock);
	}

	/**
	 * 根据当前账号坐标，克隆一个它的副本
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 比较两个账号坐标的排列顺序
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AccountDock that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		// 比较用户签名、文件编号、文件下标
		int ret = siger.compareTo(that.siger);
		if (ret == 0) {
			ret = Laxkit.compareTo(dock, that.dock);
		}
		return ret;
	}

	/**
	 * 账号坐标生成数据流输出
	 * @return 返回字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 从数据流中解析账号坐标，返回解析的长度
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