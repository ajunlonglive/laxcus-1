/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.io.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * LAXCUS类对象码。<br><br>
 * 
 * 基于MAC地址、类名、类所在目录，生成的标记值，做为每个类的唯一值。
 * 
 * @author scott.liang
 * @version 1.0 1/29/2020
 * @since laxcus 1.0
 */
public final class ClassCode implements Classable, Cloneable, Serializable, Comparable<ClassCode> {

	private static final long serialVersionUID = 8631944803878534573L;

	/** MD5散列码的高/低位 **/
	private long high;

	private long low;

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		// MD5散列码
		writer.writeLong(high);
		writer.writeLong(low);
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
		// MD5散列码
		high = reader.readLong();
		low = reader.readLong();
		// 返回读取的字节长度
		return reader.getSeek() - seek;
	}

	/**
	 * 输出字节数组
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 构造一个默认和私有的LAXCUS类对象码
	 */
	private ClassCode() {
		super();
		high = low = 0;
	}

	/**
	 * 根据传入的LAXCUS类对象码，生成它的数据副本
	 * @param that LAXCUS类对象码
	 */
	private ClassCode(ClassCode that) {
		super();
		high = that.high;
		low = that.low;
	}

	/**
	 * 构造LAXCUS类对象码，指定MD5散列码
	 * @param hash MD5散列码
	 */
	public ClassCode(MD5Hash hash) {
		this();
		setHash(hash);
	}

	/**
	 * 从可类化读取器中解析LAXCUS类对象码参数
	 * @param reader 可类化数据读取器
	 */
	public ClassCode(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 把字节数组解析成解析LAXCUS类对象码
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 */
	public ClassCode(byte[] b, int off, int len) {
		this(new ClassReader(b, off, len));
	}

	/**
	 * 把字节数组解析成解析LAXCUS类对象码
	 * @param b 字节数组
	 */
	public ClassCode(byte[] b) {
		this(b, 0, b.length);
	}

	/**
	 * 返回高位字段
	 * @return long
	 */
	public long getHigh() {
		return this.high;
	}

	/**
	 * 返回低位字段
	 * @return long
	 */
	public long getLow() {
		return this.low;
	}
	
	/**
	 * 转换成长整数
	 * @param hash MD5码
	 */
	private void convert(MD5Hash hash) {
		byte[] b = hash.get();

		// 清0
		high = low = 0;
		// 转换
		int off = 56;
		for (int i = 0; i < 8; i++, off -= 8) {
			high |= ((long) (b[i] & 0xFF) << off);
			low |= ((long) (b[i + 8] & 0xFF) << off);
		}
	}
	
	/**
	 * 设置MD5散列码，不允许空指针
	 * @param hash MD5散列码
	 */
	public void setHash(MD5Hash hash) {
		Laxkit.nullabled(hash);
		convert(hash);
	}

	/**
	 * 返回MD5散列码
	 * @return MD5散列码
	 */
	public MD5Hash getHash() {
		byte[] b = new byte[MD5Hash.volume()];
		int off = 56;
		for (int i = 0; i < 8; i++, off -= 8) {
			b[i] = (byte) ((high >>> off) & 0xFF);
			b[i + 8] = (byte) ((low >>> off) & 0xFF);
		}
		return new MD5Hash(b);
	}
	
	/**
	 * 返回字节数组
	 * @return byte[]
	 */
	public byte[] toBytes() {
		return getHash().get();
	}

	/**
	 * 生成当前实例的数据副本
	 * @return FrontCode实例
	 */
	public ClassCode duplicate() {
		return new ClassCode(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != ClassCode.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((ClassCode) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (high ^ low);
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%X%X", high, low);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ClassCode that) {
		// 空值在前
		if (that == null) {
			return 1;
		}
		// 比较参数，判断一致！
		int ret = Laxkit.compareTo(high, that.high);
		if (ret == 0) {
			ret = Laxkit.compareTo(low, that.low);
		}
		return ret;
	}

}