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
 * SHA1算法散列码 <br><br>
 * 
 * SHA1散列码应用在判断对象一致性的环境。<br>
 * 
 * @author scott.liang
 * @version 1.13 09/07/2016
 * @since laxcus 1.0
 */
public final class SHA1Hash implements Classable, Serializable, Cloneable, Markable, Comparable<SHA1Hash> {

	private static final long serialVersionUID = -4262211991994939747L;

	/** SHA1散列码，固定20个字节 **/
	private byte[] value = new byte[20];

	/** 散列码的散列值 **/
	private transient int hash;

	/**
	 * 返回固定容量尺寸：20字节
	 * @return 返回20
	 */
	public static int volume() {
		return 20;
	}

	/**
	 * 将SHA1算法散列码写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.write(value, 0, value.length);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析SHA1算法散列码
	 * @see com.laxcus.util.classable.Classable#resolve(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	public int resolve(ClassReader reader) {
		int seek = reader.getSeek();
		set(reader.read(value.length));
		return reader.getSeek() - seek;
	}

	/**
	 * 使用传入的SHA1散列码，生成它的数据副本
	 * 
	 * @param that SHA1实例
	 */
	private SHA1Hash(SHA1Hash that) {
		super();
		set(that);
	}

	/**
	 * 构造默认0值的SHA1码
	 */
	private SHA1Hash() {
		super();
		Arrays.fill(value, (byte) 0);
		hash = 0;
	}

	/**
	 * 构造SHA1对象并且设置散列码
	 * @param b SHA1散列码字节数组
	 */
	public SHA1Hash(byte[] b) {
		this();
		set(b);
	}

	/**
	 * 构造对象，用一个字节填充全部数据位
	 * @param b 单字节数据位
	 */
	public SHA1Hash(byte b) {
		this();
		for (int i = 0; i < value.length; i++) {
			value[i] = b;
		}
	}

	/**
	 * 设置一个16进制的字符串散列码
	 * 
	 * @param hex SHA1的16进制字符串
	 */
	public SHA1Hash(String hex) {
		this();
		setHexText(hex);
	}

	/**
	 * 从可类化数据读取器中解析SHA1参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SHA1Hash(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出SHA1散列参数
	 * @param reader 标记化读取器
	 */
	public SHA1Hash(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置SHA1哈希值
	 * 
	 * @param b SHA1字节数组
	 */
	private void set(byte[] b) {
		if (b == null || b.length != 20) {
			throw new IllegalValueException("must be 20 bytes");
		}
		System.arraycopy(b, 0, value, 0, value.length);
		hash = Arrays.hashCode(value);
	}

	/**
	 * 复制SHA1参数
	 * @param that SHA1实例
	 */
	private void set(SHA1Hash that) {
		// 不允许空指针
		Laxkit.nullabled(that);
		
		System.arraycopy(that.value, 0, value, 0, value.length);
		hash = that.hash;
	}

	/**
	 * 返回SHA1哈希值，20个字节
	 * 
	 * @return SHA1字节数组
	 */
	public final byte[] get() {
		return Arrays.copyOf(value, value.length);
	}

	/**
	 * 返回SHA1的16进制字符文本
	 * 
	 * @return SHA1的16进制字符串
	 */
	public String getHexText() {
		return itoh(value);
	}

	/**
	 * 把SHA1的16进制文本转为字节数组保存
	 * 
	 * @param hex SHA1的16进制字符串
	 */
	private void setHexText(String hex) {
		set(htoi(hex));
	}

	/**
	 * 字节转成16进制字符串，一定是40个字符
	 * 
	 * @param b SHA1字节数组
	 * @return 返回SHA1的16进制字符串
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
	 * 将SHA1的16进制字符串转成字节
	 * 
	 * @param input 16进制字符串
	 */
	private byte[] htoi(String input) {
		// 判断有效
		if (!SHA1Hash.validate(input)) {
			throw new IllegalValueException("illegal sha1 %s", input);
		}
		input = input.trim();
		int seek = 0;
		byte[] b = new byte[20];
		for (int index = 0; index < 20; index++) {
			String sub = input.substring(seek, seek + 2);
			b[index] = (byte) Integer.parseInt(sub, 16);
			seek += 2;
		}
		return b;
	}

	/**
	 * 根据当前SHA1散列码和传入的模数，产生它的模值
	 * @param m 整形模数
	 * @return 返回当前SHA1散列码整形模值
	 * @since 1.13
	 */
	public int mod(int m) {
		BigInteger bi = new BigInteger(1, value);
		BigInteger rs = bi.mod(BigInteger.valueOf(m));
		return rs.intValue();
	}

	/**
	 * 根据当前SHA1散列码和传入的模数，产生它的模值
	 * @param m 长整形模数
	 * @return 返回当前SHA1散列码长整形模值
	 * @since 1.13
	 */
	public long mod(long m) {
		BigInteger bi = new BigInteger(1, value);
		BigInteger rs = bi.mod(BigInteger.valueOf(m));
		return rs.longValue();
	}

	/**
	 * 返回当前SHA1Hash对象的深层副本
	 * @return SHA1实例
	 */
	public SHA1Hash duplicate() {
		return new SHA1Hash(this);
	}

	/**
	 * 判断两个SHA1散列码一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SHA1Hash.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SHA1Hash) that) == 0;
	}

	/**
	 * 返回SHA1散列值
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
	 * 返回SHA1Hash对象的深层副本（参数成员生成新的对象，而不是赋值）
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
	public int compareTo(SHA1Hash that) {
		// 空对象排前面
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(value, that.value);
	}

	/**
	 * 判断是有效的16进制SHA1散列码字符串（固定40个字符）。
	 * @param input 16进制字符串
	 * @return 匹配返回“真”，否则“假”。
	 */
	public static boolean validate(String input) {
		boolean success = (input != null);
		if (success) {
			success = input.matches("^\\s*([0-9a-fA-F]{40})\\s*$");
		}
		return success;
	}

}