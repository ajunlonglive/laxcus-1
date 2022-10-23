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
 * MD5算法散列码。 <br>
 * 
 * 用于文本/数字内容的一致性判断
 *  
 * @author scott.liang
 * @version 1.12 09/07/2016
 * @since laxcus 1.0
 */
public final class MD5Hash implements Classable, Markable, Serializable, Cloneable, Comparable<MD5Hash> {

	private static final long serialVersionUID = 1500054183925359281L;

	/** MD5散列码，固定16个字节 **/
	private byte[] value = new byte[16];

	/** 散列码的散列值 **/
	private transient int hash;

	/**
	 * 返回MD5固定容量：16字节
	 * @return int
	 */
	public static int volume() {
		return 16;
	}

	/**
	 * 将MD5算法散列码写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.write(value, 0, value.length);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析MD5算法散列码
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
	 * @param that MD5Hash实例
	 */
	private MD5Hash(MD5Hash that) {
		super();
		set(that);
	}

	/**
	 * 构造默认0值的MD5码
	 */
	private MD5Hash() {
		super();
		Arrays.fill(value, (byte) 0);
		hash = 0;
	}

	/**
	 * 构造对象并且设置散列码
	 * @param b 散列码值
	 */
	public MD5Hash(byte[] b) {
		this();
		set(b);
	}

	/**
	 * 构造对象，用一个字节填充全部数据位
	 * @param b 单字节数据位
	 */
	public MD5Hash(byte b) {
		this();
		for (int i = 0; i < value.length; i++) {
			value[i] = b;
		}
	}

	/**
	 * 设置一个16进制的字符串散列码
	 * @param hex 16进制字符串
	 */
	public MD5Hash(String hex) {
		this();
		setHexText(hex);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public MD5Hash(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出MD5散列参数
	 * @param reader 标记化读取器
	 */
	public MD5Hash(MarkReader reader) {
		this();
		reader.readObject(this);
	}

	/**
	 * 设置哈希值
	 * @param b 字节数组
	 */
	private void set(byte[] b) {
		if (b == null || b.length != 16) {
			throw new IllegalValueException("must be 16");
		}
		System.arraycopy(b, 0, value, 0, value.length);
		hash = Arrays.hashCode(value);
	}

	/**
	 * 复制参数
	 * @param that 传入实例
	 */
	private void set(MD5Hash that) {
		Laxkit.nullabled(that);

		System.arraycopy(that.value, 0, value, 0, value.length);
		hash = that.hash;
	}

	/**
	 * 返回哈希值
	 * @return MD5的字节数组
	 */
	public final byte[] get() {
		return Arrays.copyOf(value, value.length);
	}

	/**
	 * 返回16进制字符文本
	 * 
	 * @return MD5的16进制字符串
	 */
	public String getHexText() {
		return itoh(value);
	}

	/**
	 * 16进制文本转为字节数组保存
	 * @param hex 16进制字符串
	 */
	private void setHexText(String hex) {
		set(htoi(hex));
	}

	/**
	 * 字节转成16进制字符串，一定是40个字符
	 * @param b MD5字节数组
	 * @return MD5的16进制字符串
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
	 * 将16进制字符串转成字节
	 * @param input 16进制字符串
	 */
	private byte[] htoi(String input) {
		// 判断有效
		if (!MD5Hash.validate(input)) {
			throw new IllegalValueException("illegal md5 %s", input);
		}
		input = input.trim();
		int seek = 0;
		byte[] b = new byte[16];
		for (int index = 0; index < 16; index++) {
			String sub = input.substring(seek, seek + 2);
			b[index] = (byte) Integer.parseInt(sub, 16);
			seek += 2;
		}
		return b;
	}

	/**
	 * 根据当前MD5散列码和传入的模数，产生它的模值
	 * @param m 模数
	 * @return 返回当前MD5散列码模值
	 * @since 1.12
	 */
	public int mod(int m) {
		BigInteger bi = new BigInteger(1, value);
		BigInteger rs = bi.mod(BigInteger.valueOf(m));
		return rs.intValue();
	}

	/**
	 * 根据当前MD5散列码和传入的模数，产生它的模值
	 * @param m 模数
	 * @return 返回当前MD5散列码模值
	 * @since 1.12
	 */
	public long mod(long m) {
		BigInteger bi = new BigInteger(1, value);
		BigInteger rs = bi.mod(BigInteger.valueOf(m));
		return rs.longValue();
	}

	/**
	 * 返回当前MD5Hash对象的深层副本
	 * @return 当前MD5Hash的数据副本
	 */
	public MD5Hash duplicate() {
		return new MD5Hash(this);
	}

	/**
	 * 判断两个MD5散列码一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != MD5Hash.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((MD5Hash) that) == 0;
	}

	/**
	 * 返回MD5散列值
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
	 * 返回MD5Hash对象的深层副本（参数成员生成新的对象，而不是赋值）
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
	public int compareTo(MD5Hash that) {
		// 空对象排前面
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(value, that.value);
	}

	/**
	 * 判断是有效的16进制MD5散列码，固定个32字符。
	 * @param input 16进制字符串
	 * @return 匹配返回“真”，否则“假”。
	 */
	public static boolean validate(String input) {
		boolean success = (input != null);
		if (success) {
			success = input.matches("^\\s*([0-9a-fA-F]{32})\\s*$");
		}
		return success;
	}

//	public static void main(String[] args) {
//		MD5Hash h1 = Laxkit.doMD5Hash("aixbit".getBytes());
//		MD5Hash h2 = Laxkit.doMD5Hash("demo".getBytes());
//		int ret = h2.compareTo(h1);
//		System.out.printf("%s - %s is %d\n", h1, h2, ret);
//	}
}