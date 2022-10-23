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
 * 日期列，格式: yyyy-MM-dd
 * 
 * @author scott.liang
 * @version 1.0 3/15/2009
 * @since laxcus 1.0
 */
public final class Date extends Number {
	
	private static final long serialVersionUID = 7751353369412054155L;
	
	/** 日期数值 **/
	private int value;
	
	/**
	 * 构造一个默认的日期列
	 */
	public Date() {
		super(ColumnType.DATE);
	}

	/**
	 * 根据传入的日期列，生成它的副本
	 * @param that 日期列实例
	 */
	private Date(Date that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造一个日期列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public Date(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造一个日期列，并且指定它的列编号和数值
	 * @param columnId 列编号
	 * @param value 日期值
	 */
	public Date(short columnId, int value) {
		this(columnId);
		setValue(value);
	}
	
	/**
	 * 构造一个日期列，并且指定它的列编号和日期值
	 * @param columnId 列编号
	 * @param value 日期值
	 */
	public Date(short columnId, java.util.Date value) {
		this(columnId);
		setValue(value);
	}
	
	/**
	 * 设置日期值
	 * @param date
	 */
	public void setValue(java.util.Date date) {
		setValue(SimpleDate.format(date));
	}

	/**
	 * 设置日期值
	 * @param param 日期值
	 */
	public void setValue(int param) {
		value = param;
		setNull(false);
	}

	/**
	 * 返回日期值
	 * @return int
	 */
	public int getValue() {
		return this.value;
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
	 * 日期固定4个字节
	 * @see com.laxcus.access.column.Number#getNumberSize()
	 */
	@Override
	public int getNumberSize() {
		return 4;
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

		if (that.getClass() == Date.class) {
			return Laxkit.compareTo(value, ((Date) that).value);
		} else {
			String s = String.format("%s != %s", that.getClass().getName(), getClass().getName());
			throw new ClassCastException(s);
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

		if (that.getClass() == Date.class) {
			if (asc) {
				return Laxkit.compareTo(value, ((Date) that).value);
			} else {
				return Laxkit.compareTo(((Date) that).value, value);
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
	public Date duplicate() {
		return new Date(this);
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
		if (that == null || that.getClass() != com.laxcus.access.column.Date.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		Date date = (Date) that;
		if (!date.isNull() && !isNull()) {
			return value == date.value;
		}
		return date.isNull() == this.isNull();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if(isNull()) return 0;
		return value;
	}
	
	/**
	 * 返回字符串
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isNull()) {
			return null;
		}
		java.util.Date date = SimpleDate.format(value);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}
}