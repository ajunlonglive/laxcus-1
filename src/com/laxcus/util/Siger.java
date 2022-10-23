/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.io.*;
import java.util.*;

import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;
import com.laxcus.util.markable.*;

/**
 * 数字签名人 <br><br>
 * 
 * 数字签名人封装SHA256散列码，是用户账号的用户名称。<br>
 * 
 * @author scott.liang
 * @version 1.1 12/05/2013
 * @since laxcus 1.0
 */
public final class Siger implements Classable, Markable, Serializable, Cloneable, Comparable<Siger> {

	private static final long serialVersionUID = -5933650515542204161L;

	/** SHA256散列码 **/
	private SHA256Hash value;

	/**
	 * 将数字签名人写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.writeObject(value);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析数字签名人
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		value = new SHA256Hash(reader);
		return reader.getSeek() - seek;
	}

	/**
	 * 使用传入的散列码，生成它的数据副本
	 * @param that Siger实例
	 */
	private Siger(Siger that) {
		super();
		value = that.value.duplicate();
	}

	/**
	 * 构造默认的数字签名人实例
	 */
	public Siger() {
		super();
		byte[] b = new byte[SHA256Hash.volume()];
		Arrays.fill(b, (byte) 0);
		set(b);
	}

	/**
	 * 根据字节数组，生成数字签名人
	 * @param b 散列码值
	 */
	public Siger(byte[] b) {
		super();
		set(b);
	}

	/**
	 * 根据16进制的字节串散列码，生成数字签名人
	 * @param hex 16进制文本
	 */
	public Siger(String hex) {
		super();
		set(hex);
	}

	/**
	 * 根据散列码，生成数字签名人
	 * @param e SHA256散列码实例
	 */
	public Siger(SHA256Hash e) {
		super();
		set(e);
	}

	/**
	 * 从可类化数据读取器中解析数字签名人参数
	 * @param reader 可类化数据读取器
	 */
	public Siger(ClassReader reader) {
		super();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出数据签名人参数
	 * @param reader 标记化读取器
	 */
	public Siger(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置哈希值
	 * @param b SHA256字节数组
	 */
	private void set(byte[] b) {
		value = new SHA256Hash(b);
	}

	/**
	 * 16进制文本转为字节数组保存
	 * @param hex SHA256的16进制字符串
	 */
	private void set(String hex) {
		value = new SHA256Hash(hex);
	}

	/**
	 * 设置哈希值
	 * @param e SHA256实例
	 */
	private void set(SHA256Hash e) {
		Laxkit.nullabled(e);
		value = e.duplicate();
	}

	/**
	 * 返回SHA256字节数组
	 * @return byte[]
	 */
	public byte[] binary() {
		return value.get();
	}

	/**
	 * 返回SAH1对象
	 * @return SHA256Hash实例
	 */
	public SHA256Hash get() {
		return value;
	}

	/**
	 * 返回16进制字符文本
	 * 
	 * @return 16进制字符串
	 */
	public String getHex() {
		return value.getHexText();
	}

	/**
	 * 根据传入的模数，产生它的模值
	 * @param m 整形模数
	 * @return 返回整形模值
	 */
	public int mod(int m) {
		return value.mod(m);
	}

	/**
	 * 根据传入的模数，产生它的模值
	 * @param m 长整形模数
	 * @return 返回长整形模值
	 */
	public long mod(long m) {
		return value.mod(m);
	}

	/**
	 * 返回当前数字签名人的深层副本
	 * @return 当前Siger数据副本
	 */
	public Siger duplicate() {
		return new Siger(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Siger.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Siger) that) == 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate();
	}

	/**
	 * 返回16进制字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getHex();
	}

	/**
	 * 比较排序位置
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Siger that) {
		// 空对象排前面
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(value, that.value);
	}

	/**
	 * 判断是有效的16进制SHA256散列码字符串（固定64个字符）。
	 * @param input 16进制字符串
	 * @return 匹配返回“真”，否则“假”。
	 */
	public static boolean validate(String input) {
		return SHA256Hash.validate(input);
	}
}