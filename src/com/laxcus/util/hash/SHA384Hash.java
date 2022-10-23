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
 * SHA384算法散列码 <br><br>
 * 
 * SHA384是SHA512的简版。
 * 
 * @author scott.liang
 * @version 1.0 08/08/2017
 * @since laxcus 1.0
 */
public final class SHA384Hash implements Classable, Markable, Serializable, Cloneable, Comparable<SHA384Hash> {

	private static final long serialVersionUID = 698000564719561199L;

	/** SHA384散列码，固定48个字节 **/
	private byte[] value = new byte[48];

	/** 散列码的散列值 **/
	private transient int hash;
	
	/**
	 * 返回固定容量尺寸：48字节
	 * @return 48字节
	 */
	public static int volume() {
		return 48;
	}

	/**
	 * 将SHA384算法散列码写入可类化存储器
	 * @see com.laxcus.util.classable.Classable#build(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	public int build(ClassWriter writer) {
		int size = writer.size();
		writer.write(value, 0, value.length);
		return writer.size() - size;
	}

	/**
	 * 从可类化读取器中解析SHA384算法散列码
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
	 * @param that SHA384Hash实例
	 */
	private SHA384Hash(SHA384Hash that) {
		super();
		set(that);
	}

	/**
	 * 构造默认0值的SHA384码
	 */
	private SHA384Hash() {
		super();
		Arrays.fill(value, (byte) 0);
		hash = 0;
	}

	/**
	 * 构造对象并且设置散列码，48个字节
	 * @param b SHA384散列码值
	 */
	public SHA384Hash(byte[] b) {
		this();
		set(b);
	}

	/**
	 * 构造对象，用一个字节填充全部数据位
	 * @param b 单字节数据位
	 */
	public SHA384Hash(byte b) {
		this();
		for (int i = 0; i < value.length; i++) {
			value[i] = b;
		}
	}

	/**
	 * 设置一个16进制的字符串散列码
	 * @param hex 16进制字符串
	 */
	public SHA384Hash(String hex) {
		this();
		setHexText(hex);
	}

	/**
	 * 从可类化数据读取器中解析参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public SHA384Hash(ClassReader reader) {
		this();
		resolve(reader);
	}

	/**
	 * 从标记化读取器中取出SHA1散列参数
	 * @param reader 标记化读取器
	 */
	public SHA384Hash(MarkReader reader) {
		this();
		reader.readObject(this);
	}
	
	/**
	 * 设置SHA384哈希值
	 * @param b SHA384字节数组
	 */
	private void set(byte[] b) {
		if (b == null || b.length != 48) {
			throw new IllegalValueException("must be 48 bytes");
		}
		System.arraycopy(b, 0, value, 0, value.length);
		hash = Arrays.hashCode(value);
	}

	/**
	 * 复制SHA384参数
	 * @param that SHA384实例
	 */
	private void set(SHA384Hash that) {
		// 不允许空指针
		Laxkit.nullabled(that);

		System.arraycopy(that.value, 0, value, 0, value.length);
		hash = that.hash;
	}

	/**
	 * 返回SHA384哈希值
	 * @return 返回SHA384的48个字节数组
	 */
	public final byte[] get() {
		return Arrays.copyOf(value, value.length);
	}

	/**
	 * 返回SHA384的16进制字符文本
	 * 
	 * @return 返回SHA384的16进制字符串
	 */
	public String getHexText() {
		return itoh(value);
	}

	/**
	 * 16进制文本转为字节数组保存
	 * @param hex SHA384的16进制字符串
	 */
	private void setHexText(String hex) {
		set(htoi(hex));
	}

	/**
	 * 字节转成16进制字符串，一定是96个字符
	 * @param b SHA384字节数组
	 * @return 返回SHA384的16进制字符串
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
		if (!SHA384Hash.validate(input)) {
			throw new IllegalValueException("illegal sha1 %s", input);
		}
		input = input.trim();
		int seek = 0;
		byte[] b = new byte[48];
		for (int index = 0; index < 48; index++) {
			String sub = input.substring(seek, seek + 2);
			b[index] = (byte) Integer.parseInt(sub, 16);
			seek += 2;
		}
		return b;
	}

	/**
	 * 根据当前SHA384散列码和传入的模数，产生它的模值
	 * @param m 整形模数
	 * @return 返回当前SHA384散列码整形模值
	 * @since 1.13
	 */
	public int mod(int m) {
		BigInteger bi = new BigInteger(1, value);
		BigInteger rs = bi.mod(BigInteger.valueOf(m));
		return rs.intValue();
	}
	
	/**
	 * 根据当前SHA384散列码和传入的模数，产生它的模值
	 * @param m 长整形模数
	 * @return 返回当前SHA384散列码长整形模值
	 * @since 1.13
	 */
	public long mod(long m) {
		BigInteger bi = new BigInteger(1, value);
		BigInteger rs = bi.mod(BigInteger.valueOf(m));
		return rs.longValue();
	}

	/**
	 * 返回当前SHA384Hash对象的深层副本
	 * @return SHA384的数据副本
	 */
	public SHA384Hash duplicate() {
		return new SHA384Hash(this);
	}

	/**
	 * 判断两个SHA384散列码一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != SHA384Hash.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((SHA384Hash) that) == 0;
	}

	/**
	 * 返回SHA384散列值
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
	 * 返回SHA384Hash对象的深层副本（参数成员生成新的对象，而不是赋值）
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
	public int compareTo(SHA384Hash that) {
		// 空对象排前面
		if (that == null) {
			return 1;
		}
		return Laxkit.compareTo(value, that.value);
	}

	/**
	 * 判断是有效的16进制SHA384散列码字符串（固定96个字符）。
	 * @param input 16进制字符串
	 * @return 匹配返回“真”，否则“假”。
	 */
	public static boolean validate(String input) {
		boolean success = (input != null);
		if (success) {
			success = input.matches("^\\s*([0-9a-fA-F]{96})\\s*$");
		}
		return success;
	}

}