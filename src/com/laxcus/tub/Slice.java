/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub;

import java.io.*;
import java.util.*;

import com.laxcus.util.*;
import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;

/**
 * FIXP协议消息，在FIXP协议标头之后出现。一次FIXP通信有任意多组消息。<br>
 * 
 * <pre>
 * 格式组合：
 * 消息键(8位) 
 * 消息值类型(4位) 
 * 消息值长度(12位) 
 * 消息值(1-0xfff字节之间，超过非法) 
 * </pre>
 * 
 * FIXP消息中的规定：<br>
 * 1. 数值类型(INT、REAL)采用压缩格式，只取它的有效数字。如整型值的0x2010，前面的16字节被忽略，只保留后面2010。<br>
 * 2. 如果字符串类型(STRING)，文字统一采用UTF8编码。<br>
 * 
 * @author scott.liang 
 * @version 1.1 11/10/2015
 * @since laxcus 1.0
 */
//public final class Slice implements Classable, Serializable, Cloneable, Comparable<Slice> {
public final class Slice implements Serializable, Cloneable, Comparable<Slice> {
	
	private static final long serialVersionUID = 2159848309587869780L;

	/** 消息键 **/
	private short key;

	/** 消息参数类型 **/
	private byte family;

	/** 消息参数 **/
	private byte[] value;

	/**
	 * 根据传入的FIXP消息实例，生成一个它的数据副本
	 * @param that Slice实例
	 */
	private Slice(Slice that) {
		super();
		key = that.key;
		family = that.family;
		if (that.value != null && that.value.length > 0) {
			value = Arrays.copyOfRange(that.value, 0, that.value.length);
		}
	}

	/**
	 * 构造一个默认的FIXP消息
	 */
	public Slice() {
		super();
		key = 0;
		family = 0;
	}

	/**
	 * 从可类化读取器中解析出FIXP消息参数
	 * @param reader 可类化数据读取器
	 * @since 1.1
	 */
	public Slice(ClassReader reader) throws TubProtocolException {
		this();
		resolve(reader);
	}

	/**
	 * 构造FIXP消息，指定消息键值
	 * 
	 * @param key 消息键值
	 */
	public Slice(short key) {
		this();
		setKey(key);
	}

	/**
	 * 构造FIXP消息，指定消息键值和字节数组参数
	 * 
	 * @param key 消息键值
	 * @param b 参数是可变长字节数组
	 */
	public Slice(short key, byte[] b) {
		this(key);
		setRaw(b);
	}

	/**
	 * 构造FIXP消息，指定消息键值和布尔值参数
	 * 
	 * @param key 消息键值
	 * @param value 布尔值
	 */
	public Slice(short key, boolean value) {
		this(key);
		setValue(value);
	}

	/**
	 * 构造FIXP消息，指定消息键值和短整型参数
	 * 
	 * @param key 消息键值
	 * @param value 短整整值
	 */
	public Slice(short key, short value) {
		this(key);
		setValue(value);
	}

	/**
	 * 构造FIXP消息，指定消息键值和整型参数
	 * 
	 * @param key 消息键值
	 * @param value 整值
	 */
	public Slice(short key, int value) {
		this(key);
		setValue(value);
	}

	/**
	 * 构造FIXP消息，指定消息键值和长整型参数
	 * 
	 * @param key 消息键值
	 * @param value 长整值
	 */
	public Slice(short key, long value) {
		this(key);
		setValue(value);
	}

	/**
	 * 构造FIXP消息，指定消息键值和字符串参数
	 * 
	 * @param key 消息键值
	 * @param value 字符串值
	 */
	public Slice(short key, String value) {
		this(key);
		setValue(value);
	}

	/**
	 * 构造FIXP消息，指定消息键值和单浮点参数
	 * 
	 * @param key 消息键值
	 * @param value 单浮点值
	 */
	public Slice(short key, float value) {
		this(key);
		setValue(value);
	}

	/**
	 * 构造FIXP消息，指定消息键值和双浮点参数
	 * 
	 * @param key 消息键值
	 * @param value 双浮点值
	 */
	public Slice(short key, double value) {
		this(key);
		setValue(value);
	}

	/**
	 * 从输入流中解析一行消息，返回解析的长度
	 * @param input FIXP输入流句柄
	 * @return 解析的字节长度
	 * @throws IOException
	 */
	public int resolve(TubInputStream input) throws IOException {
		// 读消息前缀部分
		byte[] b = input.readFully(4);
		// 消息键值
		key = Laxkit.toShort(b, 0, 2);
		// 消息值类型+消息值长度
		short type_size = Laxkit.toShort(b, 2, 2);
		// 确定消息值类型
		family = (byte) ((type_size >>> 12) & 0xF);
		if (!SliceType.isType(family)) {
			throw new TubProtocolException("illegal family: %d", family);
		}
		// 确定消息值长度和读取
		int len = type_size & 0xFFF;
		if (len > 0) {
			value = input.readFully(len);
		}

		return 4 + len;
	}

	/**
	 * 设置消息键
	 * @param who 短整型消息键
	 */
	public void setKey(short who) {
		key = who;
	}

	/**
	 * 返回消息键
	 * @return 短整型消息键
	 */
	public short getKey() {
		return key;
	}

	/**
	 * 返回消息参数类型
	 * @return 字节消息类型
	 */
	public byte getFamily() {
		return family;
	}

	/**
	 * 判断是二进制格式
	 * @return 返回真或者假
	 */
	public boolean isRaw() {
		return SliceType.isRaw(family);
	}

	/**
	 * 判断是字符串格式
	 * @return 返回真或者假
	 */
	public boolean isString() {
		return SliceType.isString(family);
	}

	/**
	 * 判断是布尔格式
	 * @return 返回真或者假
	 */
	public boolean isBoolean() {
		return SliceType.isBoolean(family);
	}

	/**
	 * 判断是短整型格式
	 * @return 返回真或者假
	 */
	public boolean isShort() {
		return SliceType.isShort(family);
	}

	/**
	 * 判断是整型格式
	 * @return 返回真或者假
	 */
	public boolean isInteger() {
		return SliceType.isInteger(family);
	}

	/**
	 * 判断是长整型格式
	 * @return 返回真或者假
	 */
	public boolean isLong() {
		return SliceType.isLong(family);
	}

	/**
	 * 判断是单浮点格式
	 * @return 返回真或者假
	 */
	public boolean isFloat() {
		return SliceType.isFloat(family);
	}

	/**
	 * 判断是双浮点格式
	 * @return 返回真或者假
	 */
	public boolean isDouble() {
		return SliceType.isDouble(family);
	}

	/**
	 * 设置消息值
	 * @param who 消息数据类型
	 * @param b  字节数组
	 * @param off 数组下标
	 * @param len 长度
	 */
	private void setValue(byte who, byte[] b, int off, int len) {
		if (len > 0xFFF) {
			throw new IllegalArgumentException("illegal size");
		}
		family = who;
		value = Arrays.copyOfRange(b, off, off + len);
	}

	/**
	 * 返回原始值
	 * @return 字节数组
	 */
	public byte[] getValue() {
		return value;
	}

	/**
	 * 返回二进制字节数组
	 * @return 字节数组
	 */
	public byte[] getRaw() {
		return value;
	}

	/**
	 * 设置可变长字节数组
	 * @param value 字节数组
	 */
	public void setRaw(byte[] value) {
		if (value == null || value.length == 0) {
			throw new NullPointerException();
		}
		setValue(SliceType.RAW, value, 0, value.length);
	}

	/**
	 * 设置布尔值
	 * @param value 布尔值
	 */
	public void setValue(boolean value) {
		byte[] b = new byte[1];
		b[0] = (byte) (value ? 1 : 0);
		setValue(SliceType.BOOLEAN, b, 0, b.length);
	}

	/**
	 * 返回布尔值
	 * @return 布尔值
	 */
	public boolean getBoolean() {
		return (value != null && value[0] == 1);
	}

	/**
	 * 设置短整型值
	 * 
	 * @param value short值
	 */
	public void setValue(short value) {
		byte[] b = Laxkit.toBytes(value, true);
		setValue(SliceType.INT16, b, 0, b.length);
	}

	/**
	 * 返回短整形值
	 * 
	 * @return short值
	 */
	public short getShort() {
		return Laxkit.toShort(value, 0, value.length);
	}

	/**
	 * 设置整型值，采用压缩格式
	 * @param value int值
	 */
	public void setValue(int value) {
		byte[] b = Laxkit.toBytes(value, true);
		setValue(SliceType.INT32, b, 0,b.length);
	}

	/**
	 * 返回整型值
	 * @return int值
	 */
	public int getInteger() {
		return Laxkit.toInteger(value, 0, value.length);
	}

	/**
	 * 设置长整型。采用压缩格式，即前面有0时忽略，只取有效的数字。
	 * @param value long值
	 */
	public void setValue(long value) {
		byte[] b = Laxkit.toBytes(value, true);
		setValue(SliceType.INT64, b, 0, b.length);
	}

	/**
	 * 返回长整型
	 * @return long值
	 */
	public long getLong() {
		return Laxkit.toLong(value, 0, value.length);
	}

	/**
	 * 设置字符串，全部采用UTF8编码
	 * @param value String值
	 */
	public void setValue(String value) {
		if (value == null || value.length() == 0) {
			throw new NullPointerException();
		}
		try {
			byte[] b = new UTF8().encode(value);
			setValue(SliceType.STRING, b, 0, b.length);
		} catch (CharsetException e) {

		}
	}

	/**
	 * 返回字符串值
	 * @return String值
	 */
	public String getString() {
		try {
			return new UTF8().decode(value, 0, value.length);
		} catch (CharsetException e) {

		}
		return null;
	}

	/**
	 * 设置单浮点值
	 * @param value float值
	 */
	public void setValue(float value) {
		int num = Float.floatToIntBits(value);
		byte[] b = Laxkit.toBytes(num, true);
		setValue(SliceType.REAL32, b,0, b.length);
	}

	/**
	 * 返回单浮点值
	 * @return float值
	 */
	public float getFloat() {
		int num = Laxkit.toInteger(value, 0, value.length);
		return Float.intBitsToFloat(num);
	}

	/**
	 * 设置双浮点值
	 * @param value double值
	 */
	public void setValue(double value) {
		long num = Double.doubleToLongBits(value);
		byte[] b = Laxkit.toBytes(num, true);
		setValue(SliceType.REAL64, b,0, b.length);
	}

	/**
	 * 返回双浮点值
	 * @return double值
	 */
	public double getDouble() {
		long num = Laxkit.toLong(value, 0, value.length);
		return Double.longBitsToDouble(num);
	}
	
	/**
	 * 返回当前消息的数据副本
	 * @return Slice实例
	 */
	public Slice duplicate() {
		return new Slice(this);
	}

	/**
	 * 返回消息的字符串格式描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = String.format("%d(%d)", key, family);
		switch (family) {
		case SliceType.RAW:
			return String.format("%s %s", s, Arrays.toString(value));
		case SliceType.STRING:
			return String.format("%s %s", s, getString());
		case SliceType.BOOLEAN:
			return String.format("%s %s", s, getBoolean());
		case SliceType.INT16:
			return String.format("%s %d", s, getShort());
		case SliceType.INT32:
			return String.format("%s %d", s, getInteger());
		case SliceType.INT64:
			return String.format("%s %d", s, getLong());
		case SliceType.REAL32:
			return String.format("%s %f", s, getFloat());
		case SliceType.REAL64:
			return String.format("%s %f", s, getDouble());
		}
		return Arrays.toString(value);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Slice.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		return compareTo((Slice) that) == 0;
	}

	/**
	 * 返回消息的散列码
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return key ^ family ^ Arrays.hashCode(value);
	}

	/**
	 * 根据当前消息实例，克隆一个它的数据副本
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
	public int compareTo(Slice that) {
		// 空对象排在前面，有效对象排在后面
		if(that == null) {
			return 1;
		}

		int ret = Laxkit.compareTo(key, that.key);
		if (ret == 0) {
			ret = Laxkit.compareTo(family, that.family);
		}
		if (ret == 0) {
			ret = Laxkit.compareTo(value, that.value);
		}
		return ret;
	}

	/**
	 * 将FIXP消息参数写入可类化存储器
	 * @param writer
	 * @return
	 */
	public int build(ClassWriter writer) {
		final int scale = writer.size();
		final int len = (value == null ? 0 : value.length);
		// 1. 设置键值
		writer.writeShort(key);
		// 2. 合并消息值参数类型和长度定义
		short type_size = family;
		type_size = (short) (((type_size & 0xF) << 12) | (len & 0xFFF));
		// 标记值(消息类型+值长度，不压缩)
		writer.writeShort(type_size);
		// 3. 参数
		if (len > 0) {
			writer.write(value);
		}
		// 返回写入长度
		return writer.size() - scale;
	}

	/**
	 * 从可类化读取器中解析FIXP消息参数
	 * @param reader
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws TubProtocolException
	 */
	public int resolve(ClassReader reader) throws IndexOutOfBoundsException, TubProtocolException {
		final int scale = reader.getSeek();
		// 检查尺寸
		if (reader.getSeek() + 4 > reader.getEnd()) {
			throw new IndexOutOfBoundsException("message size missing");
		}
		// 消息键值
		key = reader.readShort();
		// 数组类型和值长度(占16个字节，其中前4位是值类型，后12位是值长度)
		short type_size = reader.readShort();
		// 值类型
		family = (byte) ((type_size >>> 12) & 0xF);
		if (!SliceType.isType(family)) {
			throw new TubProtocolException("illegal family: %d", family);
		}
		// 值长度
		int len = type_size & 0xFFF;
		if (reader.getSeek() + len > reader.getEnd()) {
			throw new IndexOutOfBoundsException("message size missing");
		}
		// 设置参数
		if (len > 0) {
			value = reader.read(len);
		}
		// 返回解析长度
		return reader.getSeek() - scale;
	}

	/**
	 * 将消息参数转换成字节格式
	 * @return 字节数组
	 */
	public byte[] build() {
		ClassWriter writer = new ClassWriter();
		build(writer);
		return writer.effuse();
	}

	/**
	 * 解析一行消息，返回解析的长度
	 * @param b 字节数组
	 * @param off 下标
	 * @param len 长度
	 * @return 返回解析长度
	 */
	public int resolve(byte[] b, int off, int len) throws IndexOutOfBoundsException, TubProtocolException {
		ClassReader reader = new ClassReader(b, off, len);
		return resolve(reader);
	}

}