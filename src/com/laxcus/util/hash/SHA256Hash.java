/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.hash;

import java.io.*;
import java.math.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.markable.*;

/**
 * SHA256算法散列码 <br><br>
 * 
 * SHA1散列码应用范围：<br>
 * 1. 用户账号的“用户名称”的签名<br>
 * 2. 需要一致性判断的对象 <br>
 * 
 * @author scott.liang
 * @version 1.13 09/07/2016
 * @since laxcus 1.0
 */
public final class SHA256Hash implements Classable, Markable, Serializable, Cloneable, Comparable<SHA256Hash> {

	private static final long serialVersionUID = -3903830076751109508L;

	/** SHA256散列码，固定32个字节 **/
	private byte[] value = new byte[32];

	/** 散列码的散列值 **/
	private transient int hash;
	
	/**
	 * 返回固定容量尺寸：32字节
	 * @return 32字节
	 */
	public static int volume() {
		return 32;
	}

	/**
	 * 将SHA256算法散列码写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.write(value, 0, value.length);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析SHA256算法散列码
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		set(reader.read(value.length));
		return reader.getSeek() - seek;
	}

	/**
	 * 使用传入的散列码，生成它的数据副本
	 * @param that SHA256Hash实例
	 */
	private SHA256Hash(SHA256Hash that) {
		super();
		set(that);
	}

	/**
	 * 构造默认0值的SHA256码
	 */
	private SHA256Hash() {
		super();
		Arrays.fill(value, (byte) 0);
		hash = 0;
	}

	/**
	 * 构造对象并且设置散列码
	 * @param b 散列码值
	 */
	public SHA256Hash(byte[] b) {
		this();
		set(b);
	}

	/**
	 * 构造对象，用一个字节填充全部数据位
	 * @param b 单字节数据位
	 */
	public SHA256Hash(byte b) {
		this();
		for (int i = 0; i < value.length; i++) {
			value[i] = b;
		}
	}

	/**
	 * 设置一个16进制的字符串散列码
	 * @param hex 16进制字符串
	 */
	public SHA256Hash(String hex) {
		this();
		setHexText(hex);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SHA256Hash(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出SHA256散列参数
	 * @param reader 标记化读取器
	 */
	public SHA256Hash(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置哈希值
	 * @param b SHA256字节数组
	 */
	private void set(byte[] b) {
		if (b == null || b.length != 32) {
			throw new IllegalValueException("must be 32 bytes");
		}
		System.arraycopy(b, 0, value, 0, value.length);
		hash = Arrays.hashCode(value);
	}

	/**
	 * 复制SHA256参数
	 * @param that SHA256实例
	 */
	private void set(SHA256Hash that) {
		// 不允许空指针
		Laxkit.nullabled(that);

		System.arraycopy(that.value, 0, value, 0, value.length);
		hash = that.hash;
	}

	/**
	 * 返回SHA256的哈希值
	 * @return 32个字节数组
	 */
	public final byte[] get() {
		return Arrays.copyOf(value, value.length);
	}

	/**
	 * 返回SHA256的16进制字符文本
	 * 
	 * @return SHA256的16进制字符串
	 */
	public String getHexText() {
		return itoh(value);
	}

	/**
	 * SHA256的16进制文本转为字节数组保存
	 * 
	 * @param hex SHA256的16进制字符串
	 */
	private void setHexText(String hex) {
		set(htoi(hex));
	}

	/**
	 * SHA256的字节转成16进制字符串，一定是64个字符
	 * 
	 * @param b SHA256字节数组
	 * @return 返回16进制字符串
	 */
	private String itoh(byte[] b) {
		StringBuilder bf = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			String sub = String.format("%X", b[i] & 0xFF);
			if (sub.length() == 1) {
				bf.append('0');
			}
			bf.append(sub);
		}
		return bf.toString();
	}

	/**
	 * 将SHA256的16进制字符串转成字节
	 * 
	 * @param input SHA256的16进制字符串
	 */
	private byte[] htoi(String input) {
		// 判断有效
		if (!SHA256Hash.validate(input)) {
			throw new IllegalValueException("illegal sha1 %s", input);
		}
		input = input.trim();
		int seek = 0;
		byte[] b = new byte[32];
		for (int index = 0; index < 32; index++) {
			String sub = input.substring(seek, seek + 2);
			b[index] = (byte) Integer.parseInt(sub, 16);
			seek += 2;
		}
		return b;
	}

	/**
	 * 根据当前SHA256散列码和传入的模数，产生它的模值
	 * @param m 整形模数
	 * @return 返回当前SHA256散列码整形模值
	 * @since 1.13
	 */
	public int mod(int m) {
		BigInteger bi = new BigInteger(1, value);
		BigInteger rs = bi.mod(BigInteger.valueOf(m));
		return rs.intValue();
	}
	
	/**
	 * 根据当前SHA256散列码和传入的模数，产生它的模值
	 * @param m 长整形模数
	 * @return 返回当前SHA256散列码长整形模值
	 * @since 1.13
	 */
	public long mod(long m) {
		BigInteger bi = new BigInteger(1, value);
		BigInteger rs = bi.mod(BigInteger.valueOf(m));
		return rs.longValue();
	}

	/**
	 * 返回当前SHA256Hash对象的深层副本
	 * @return SHA256Hash实例
	 */
	public SHA256Hash duplicate() {
		return new SHA256Hash(this);
	}

	/**
	 * 判断两个SHA256散列码一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SHA256Hash.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SHA256Hash) that) == 0;
	}

	/**
	 * 返回SHA256散列值
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = Arrays.hashCode(value);
		}
		return hash;
	}

	/**
	 * 返回SHA256Hash对象的深层副本（参数成员生成新的对象，而不是赋值）
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
		return getHexText();
	}

	/**
	 * 比较排序位置
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SHA256Hash that) {
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
		boolean success = (input != null);
		if (success) {
			success = input.matches("^\\s*([0-9a-fA-F]{64})\\s*$");
		}
		return success;
	}

}