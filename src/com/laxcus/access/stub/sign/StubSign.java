/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.stub.sign;

import java.io.*;

import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 数据块签名。<br><br>
 * 
 * 用来标记一个数据块唯一性，参数包括：<br>
 * 1. 数据块编号<br>
 * 2. 数据块状态（三种，缓存块、存储块、缓存映像块）<br>
 * 3. 磁盘最后修改时间<br>
 * 4. 数据块MD5散列码 <br>
 * 
 * @author scott.liang
 * @version 1.0 9/12/2016
 * @since laxcus 1.0
 */
public final class StubSign implements Classable, Cloneable, Serializable, Comparable<StubSign> {

	private static final long serialVersionUID = 5815976429360261293L;

	/** 数据块编号 **/
	private long stub;

	/** 数据块状态 **/
	private byte status;

	/** 最后修改时间  **/
	private long lastModified;

	/** 散列码 **/
	private MD5Hash hash;
	
	/**
	 * 返回它的固定尺寸
	 * @return  整型数33
	 */
	public static int volume() {
		return 33;
	}

	/**
	 * 构造默认和私有的数据块签名
	 */
	public StubSign() {
		super();
	}

	/**
	 * 建立一个数据块签名的数据副本
	 * @param that StubSign实例
	 */
	private StubSign(StubSign that) {
		stub = that.stub;
		status = that.status;
		lastModified = that.lastModified;
		hash = that.hash.duplicate();
	}

	/**
	 * 构造数据块签名，指定全部参数
	 * @param stub 数据块编号
	 * @param status 数据块状态
	 * @param lastModified 最后修改时间
	 * @param hash MD5散列码
	 */
	public StubSign(long stub, byte status, long lastModified, MD5Hash hash) {
		this();
		setStub(stub);
		setStatus(status);
		setLastModified(lastModified);
		setHash(hash);
	}

	/**
	 * 从可类化数据读取器中解析数据块签名
	 * @param reader  可类化数据读取器
	 */
	public StubSign(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 设置数据块编号
	 * @param e 数据块编号
	 */
	public void setStub(long e) {
		stub = e;
	}

	/**
	 * 返回数据块编号
	 * @return 数据块编号
	 */
	public long getStub() {
		return stub;
	}

	/**
	 * 设置数据块状态
	 * @param who  数据块状态
	 */
	public void setStatus(byte who) {
		if (!MassStatus.isFamily(who)) {
			throw new IllegalValueException("illegal status:%d", who);
		}
		status = who;
	}

	/**
	 * 返回数据块状态
	 * @return  数据块状态的字节描述
	 */
	public byte getStatus() {
		return status;
	}

	/**
	 * 判断是存储块状态
	 * @return  返回真或者假
	 */
	public boolean isChunk() {
		return MassStatus.isChunk(getStatus());
	}

	/**
	 * 判断是缓存块状态
	 * @return  返回真或者假
	 */
	public boolean isCache() {
		return MassStatus.isCache(getStatus());
	}

	/**
	 * 判断是缓存映像块状态
	 * @return  返回真或者假
	 */
	public boolean isCacheReflex() {
		return MassStatus.isCacheReflex(getStatus());
	}

	/**
	 * 设置最后修改时间
	 * @param i 最后修改时间
	 */
	public void setLastModified(long i) {
		lastModified = i;
	}

	/**
	 * 返回最后修改时间
	 * @return 最后修改时间
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * 设置MD5散列码
	 * @param e MD5码
	 */
	public void setHash(MD5Hash e) {
		Laxkit.nullabled(e);

		hash = e;
	}

	/**
	 * 返回MD5散列码
	 * @return MD5码
	 */
	public MD5Hash getHash() {
		return hash;
	}

	/**
	 * 生成当前实例的数据副本
	 * @return StubSign实例
	 */
	public StubSign duplicate() {
		return new StubSign(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != StubSign.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((StubSign) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (stub >>> 32 ^ stub);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StubSign that) {
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(stub, that.stub);
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
	 * @see com.laxcus.util.Classable#build(com.laxcus.util.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		final int scale = writer.size();
		writer.writeLong(stub);
		writer.write(status);
		writer.writeLong(lastModified);
		writer.writeObject(hash);
		return writer.size() - scale;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.Classable#resolve(com.laxcus.util.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		final int seek = reader.getSeek();
		stub = reader.readLong();
		status = reader.read();
		lastModified = reader.readLong();
		hash = new MD5Hash(reader);
		return reader.getSeek() - seek;
	}

}