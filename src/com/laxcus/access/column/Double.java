/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import com.laxcus.access.column.attribute.*;
import com.laxcus.access.type.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.hash.*;

/**
 * 双浮点列。
 * 
 * @author scott.liang
 * @version 1.0 3/13/2009
 * @since laxcus 1.0
 */
public final class Double extends Number {

	private static final long serialVersionUID = -2707497851999302835L;

	/** 双浮点数值 **/
	private double value;

	/**
	 * 根据传入的双浮点列参数，生成它的副本
	 * @param that
	 */
	private Double(Double that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造一个默认的双浮点列
	 */
	public Double() {
		super(ColumnType.DOUBLE);
		value = 0.0f;
	}

	/**
	 * 构造双浮点列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public Double(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造双浮点列，并且指定它的列编号和数值
	 * @param columnId 列编号
	 * @param value 数值
	 */
	public Double(short columnId, double value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 设置双浮点列数值
	 * @param param 数值
	 */
	public void setValue(double param) {
		value = param;
		setNull(false);
	}

	/**
	 * 返回双浮点列数值
	 * @return 双浮点值
	 */
	public double getValue() {
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
		long num = Laxkit.toLong(b, off, len);
		setValue(java.lang.Double.longBitsToDouble(num));
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Number#getNumber()
	 */
	@Override
	public byte[] getNumber() {
		long num = java.lang.Double.doubleToLongBits(value);
		return Laxkit.toBytes(num);
	}

	/**
	 * 双浮点数固定8个字节
	 * @see com.laxcus.access.column.Number#getNumberSize()
	 */
	@Override
	public int getNumberSize() {
		return 8;
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

		if (that.getClass() == com.laxcus.access.column.Double.class) {
			return Laxkit.compareTo(value, ((com.laxcus.access.column.Double) that).value);
		} else if (that.getClass() == com.laxcus.access.column.Float.class) {
			return Laxkit.compareTo(value, ((com.laxcus.access.column.Float) that).getValue());
		} else {
			String str = String.format("%s != %s", getClass().getName(), that.getClass().getName());
			throw new ClassCastException(str);
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

		if (that.getClass() == com.laxcus.access.column.Double.class) {
			if(asc){
				return Laxkit.compareTo(value, ((com.laxcus.access.column.Double) that).value);
			} else {
				return Laxkit.compareTo( ((com.laxcus.access.column.Double) that).value, value);
			}
		} else if (that.getClass() == com.laxcus.access.column.Float.class) {
			if(asc) {
				return Laxkit.compareTo(value, ((com.laxcus.access.column.Float) that).getValue());
			} else {
				return Laxkit.compareTo( ((com.laxcus.access.column.Float) that).getValue(), value);
			}
		} else {
			String str = String.format("%s != %s", getClass().getName(), that.getClass().getName());
			throw new ClassCastException(str);
		}
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public Double duplicate() {
		return new Double(this); 
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
		writer.writeDouble(value);
		return new SHA256Hash(writer.effuse());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object that) {
		if (that == null || that.getClass() != com.laxcus.access.column.Double.class) {
			return false;
		} else if(that == this) {
			return true;
		}

		Double s = (Double) that;
		if (!s.isNull() && !isNull()) {
			return value == s.value;
		}
		return s.isNull() == isNull();
	}

	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if(isNull()) return 0;
		return (int) (value);
	}

	/**
	 * 返回双浮点列字符串描述
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isNull()) {
			return null;
		}
		return String.format("%f", value);
	}
}