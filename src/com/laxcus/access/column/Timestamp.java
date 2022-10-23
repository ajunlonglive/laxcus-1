/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import java.text.*;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.datetime.*;
import com.laxcus.util.hash.*;

/**
 * 日期时间(时间戳)组合。<br>
 * 格式:yyyy-MM-dd hh:mm:ss SSS
 * 
 * @author scott.liang
 * @version 1.0 3/15/2009
 * @since laxcus 1.0
 */
public final class Timestamp extends Number {

	private static final long serialVersionUID = -1524222148807349241L;

	/** 时间戳数值 **/
	private long value;

	/**
	 * 构造一个默认的时间戳列
	 */
	public Timestamp() {
		super(ColumnType.TIMESTAMP);
		value = 0L;
	}

	/**
	 * 根据传入的时间戳列，生成它的副本
	 * @param that
	 */
	private Timestamp(Timestamp that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造一个时间戳列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public Timestamp(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造一个时间戳列，并且指定它的列编号和时间戳值
	 * @param columnId 列编号
	 * @param value 时间戳值
	 */
	public Timestamp(short columnId, long value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 构造一个时间戳列，并且指定它的列编号和时间戳值
	 * @param columnId 列编号
	 * @param value 时间戳值
	 */
	public Timestamp(short columnId, java.util.Date value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 设置时间戳值
	 * @param param 时间戳值
	 */
	public void setValue(long param) {
		value = param;
		setNull(false);
	}

	/**
	 * 设置时间戳值
	 * @param date
	 */
	public void setValue(java.util.Date date) {
		setValue(SimpleTimestamp.format(date));
	}

	/**
	 * 返回时间戳值
	 * @return long
	 */
	public long getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Number#setNumber(byte[], int, int)
	 */
	@Override
	public void setNumber(byte[] b, int off, int len) {
		if (len != getNumberSize()) {
			throw new IllegalValueException("size is %d", getNumberSize());
		}
		setValue(Laxkit.toLong(b, off, len));
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Number#getNumber()
	 */
	@Override
	public byte[] getNumber() {
		return Laxkit.toBytes(value);
	}

	/**
	 * 固定8个字节
	 * @see com.laxcus.access.column.Number#getNumberSize()
	 */
	@Override
	public int getNumberSize() {
		return 8;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#compare(com.laxcus.access.column.Column)
	 */
	@Override
	public int compare(Column that) {
		if(that == null) return 1;
		else if (isNull() && that.isNull()) return 0;
		else if (isNull()) return -1;
		else if (that.isNull()) return 1;

		if (that.getClass() == Timestamp.class) {
			return Laxkit.compareTo(value, ((Timestamp) that).value);
		} else {
			throw new ClassCastException();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#compare(com.laxcus.access.column.Column, boolean)
	 */
	@Override
	public int compare(Column that, boolean asc) {
		if(that == null) return 1;
		else if (isNull() && that.isNull()) return 0;
		else if (isNull()) return -1;
		else if (that.isNull()) return 1;

		if (that.getClass() == Timestamp.class) {
			if(asc){
				return Laxkit.compareTo(value, ((Timestamp) that).value);
			} else {
				return Laxkit.compareTo(((Timestamp) that).value, value);
			}
		} else {
			throw new ClassCastException();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public Timestamp duplicate() {
		return new Timestamp(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#capacity()
	 */
	@Override
	public int capacity() {
		if(isNull()) return 1;
		return 9;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#hash(com.laxcus.access.column.attribute.ColumnAttribute)
	 */
	@Override
	public SHA256Hash hash(ColumnAttribute attribute) {
		if (isNull()) {
			return new SHA256Hash((byte) 0);
		}

		// 解析结果
		ClassWriter writer = new ClassWriter();
		writer.writeLong(value);
		return new SHA256Hash(writer.effuse());
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != com.laxcus.access.column.Timestamp.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		Timestamp stamp = (Timestamp) that;
		if (!stamp.isNull() && !isNull()) {
			return value == stamp.value;
		}
		return isNull() == stamp.isNull();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if(isNull()) {
			return 0;
		}
		return (int) (value >>> 32 ^ value);
	}

	/**
	 * 返回时间戳的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isNull()) {
			return null;
		}
		java.util.Date date = SimpleTimestamp.format(value);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		return sdf.format(date);
	}
}