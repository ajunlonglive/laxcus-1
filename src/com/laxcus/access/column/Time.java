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
 * 日期列。<br>
 * 格式: hh:mm:ss SSS
 * 
 * @author scott.liang
 * @version 1.0 3/15/2009
 * @since laxcus 1.0
 */
public final class Time extends Number {
	
	private static final long serialVersionUID = -4431947912316956634L;
	
	/** 时间数值 **/
	private int value;

	/**
	 * 构造一个默认的时间列
	 */
	public Time() {
		super(ColumnType.TIME);
	}
	
	/**
	 * 根据传入时间列，生成它的副本
	 * @param that Time实例
	 */
	private Time(Time that) {
		super(that);
		value = that.value;
	}
	
	/**
	 * 构造一个时间列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public Time(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造一个时间列，并且指定它的列编号和时间值
	 * @param columnId 列编号
	 * @param value 时间值
	 */
	public Time(short columnId, int value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 构造一个时间列，并且指定它的列编号和时间值
	 * @param columnId 列编号
	 * @param value 时间值
	 */
	public Time(short columnId, java.util.Date value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 设置时间值
	 * @param date
	 */
	public void setValue(java.util.Date date) {
		setValue(SimpleTime.format(date));
	}

	/**
	 * 设置时间列数值
	 * @param param 数值
	 */
	public void setValue(int param) {
		value = param;
		setNull(false);
	}

	/**
	 * 返回时间列数值
	 * @return 时间的int值
	 */
	public int getValue() {
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
		setValue(Laxkit.toInteger(b, off, len));
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
	 * 固定4个字节
	 * @see com.laxcus.access.column.Number#getNumberSize()
	 */
	@Override
	public int getNumberSize() {
		return 4;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.column.Column#compare(com.laxcus.access.column.Column)
	 */
	@Override
	public int compare(Column that) {
		if(that == null) return 1;
		else if (isNull() && that.isNull()) return 0;
		else if (isNull()) return -1;
		else if (that.isNull()) return 1;

		if (that.getClass() == Time.class) {
			return Laxkit.compareTo(value, ((Time) that).value);
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

		if (that.getClass() == Time.class) {
			if(asc){
				return Laxkit.compareTo(value, ((Time) that).value);
			} else {
				return Laxkit.compareTo( ((Time) that).value, value);
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
	public Time duplicate() {
		return new Time(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#capacity()
	 */
	@Override
	public int capacity() {
		if(isNull()) return 1;
		return 5;
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
		writer.writeInt(value);
		return new SHA256Hash(writer.effuse());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != com.laxcus.access.column.Time.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		
		Time time = (Time) that;
		if (!time.isNull() && !isNull()) {
			return value == time.value;
		}
		return isNull() == time.isNull();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if(isNull()) return 0;
		return value;
	}
	
	/*
	 * 返回时间的字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isNull()) return null;
		java.util.Date date = SimpleTime.format(value);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss SSS");
		return sdf.format(date);
	}
}