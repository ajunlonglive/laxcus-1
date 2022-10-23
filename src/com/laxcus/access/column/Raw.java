/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import java.io.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.type.*;
import com.laxcus.access.util.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 可变长的二进制数组类型，范围从0-2G
 * 
 * @author scott.liang
 * @version 1.0 3/12/2009
 * @since laxcus 1.0
 */
public final class Raw extends Variable {

	private static final long serialVersionUID = 8807751017024847337L;

	/**
	 * 构造一个二进制数据列
	 */
	public Raw() {
		super(ColumnType.RAW);
	}

	/**
	 * 根据传入参数构造二进制数组列的副本
	 * @param that Raw实例
	 */
	private Raw(Raw that) {
		super(that);
	}

	/**
	 * 构造二进制数组列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public Raw(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造二进制数组列，并且指定它的列编号和数值
	 * @param columnId 列编号
	 * @param value 数值
	 */
	public Raw(short columnId, byte[] value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 构造二进制数组列，并且指定它的列编号、数值、索引值
	 * @param columnId 列编号
	 * @param value 数值
	 * @param index 索引值
	 */
	public Raw(short columnId, byte[] value, byte[] index) {
		this(columnId, value);
		setIndex(index);
	}

	/**
	 * 根据当前二进制数组列，生成它的副本
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public Raw duplicate() {
		return new Raw(this);
	}

	/**
	 * 比较两个二进制数组列是否一致
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != Raw.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		return super.equals((Raw) that);
	}

	/**
	 * 在提供<b>包装属性、是否忽略索引</b>的前提下，对两个二进制数组列进行字节序列比较。
	 * 如果索引有效首先比较索引，否则比较数值。
	 * @param that 被比较列
	 * @param packing 封包属性
	 * @param ignoreIndex IS TRUE，忽略索引
	 * @return 返回排序值
	 */
	public int compare(Raw that, Packing packing, boolean ignoreIndex) {
		if(that == null) return 1;
		else if (isNull() && that.isNull()) return 0;
		else if (isNull()) return -1;
		else if (that.isNull()) return 1;

		byte[] b1 = null;
		byte[] b2 = null;
		// 如果索引存在，并且不忽略索引时
		if (!ignoreIndex && index != null && that.index != null) {
			b1 = index;
			b2 = that.index;
		} else {
			if (value == null || value.length == 0) return -1;
			if (that.value == null || that.value.length == 0) return 1;
			b1 = value;
			b2 = that.value;
		}

		// 解包
		if (packing != null && packing.isEnabled()) {
			try {
				b1 = VariableGenerator.depacking(packing, b1, 0, b1.length);
				b2 = VariableGenerator.depacking(packing, b2, 0, b2.length);
			} catch (IOException e) {
				Logger.error(e);
				return -1;
			}
		}

		// 按照字节顺序比较
		return Laxkit.compareTo(b1, b2);
	}

	/**
	 * 返回二进制字节数组的16进制字符串格式
	 * @param packing 封包
	 * @param limit 限制长度，可以是-1
	 * @return 转换成16进制后的字符串
	 */
	public String toString(Packing packing, int limit) {
		if (isNull()) {
			return null;
		}

		int max = 256;

		// 返回解包后的数据流
		byte[] b = super.getValue(packing);
		// 生成16进制字符流
		StringBuilder buff = new StringBuilder("0x");
		for (int i = 0; i < b.length; i++) {
			String s = String.format("%X", b[i] & 0xFF);
			if (s.length() == 1) {
				buff.append('0');
				buff.append(s);
			} else {
				buff.append(s);
			}
			if (limit > 0 && i + 1 == limit) {
				buff.append("...");
				break;
			} else if (i + 1 >= max) {
				buff.append("...");
				break;
			}
		}
		return buff.toString();
	}

	/**
	 * 返回二进制字节数组的字符串格式
	 * @param packing 封包
	 * @return 转换后的字符串
	 */
	public String toString(Packing packing) {
		return toString(packing, -1);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(null, -1);
	}
}