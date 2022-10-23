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
 * 单浮点列
 * 
 * @author scott.liang
 * @version 1.0 3/13/2009
 * @since laxcus 1.0
 */
public final class Float extends Number {
	
	private static final long serialVersionUID = 4109595291566765040L;

	/** 单浮点数值**/
	private float value;

	/**
	 * 构造一个默认的单浮点列
	 */
	public Float() {
		super(ColumnType.FLOAT);
		value = 0.0f;
	}
	
	/**
	 * 根据传入的单浮点列参数，生成它的副本
	 * @param that
	 */
	private Float(Float that) {
		super(that);
		value = that.value;
	}
	
	/**
	 * 构造一个单浮点列，并且指定它的列编号
	 * @param columnId
	 */
	public Float(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造一个单浮点列，并且指定它的列编号和数值
	 * @param columnId 列编号
	 * @param value 单浮点
	 */
	public Float(short columnId, float value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 设置单浮点列数值
	 * @param param 浮点值
	 */
	public void setValue(float param) {
		value = param;
		setNull(false);
	}

	/**
	 * 返回单浮点列数值
	 * @return 单浮点值
	 */
	public float getValue() {
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
		int num = Laxkit.toInteger(b, off, len);
		setValue(java.lang.Float.intBitsToFloat(num));
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Number#getNumber()
	 */
	@Override
	public byte[] getNumber() {
		int num = java.lang.Float.floatToIntBits(value);
		return Laxkit.toBytes(num);
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
		
		if (that.getClass() == com.laxcus.access.column.Float.class) {
			return Laxkit.compareTo(value, ((com.laxcus.access.column.Float) that).value);
		} else if (that.getClass() == com.laxcus.access.column.Double.class) {
			return Laxkit.compareTo(value, ((com.laxcus.access.column.Double) that).getValue());
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
		if (that == null)
			return 1;
		else if (isNull() && that.isNull())
			return 0;
		else if (isNull())
			return -1;
		else if (that.isNull())
			return 1;

		if (that.getClass() == com.laxcus.access.column.Float.class) {
			if (asc) {
				return Laxkit.compareTo(value, ((com.laxcus.access.column.Float) that).value);
			} else {
				return Laxkit.compareTo(((com.laxcus.access.column.Float) that).value, value);
			}
		} else if (that.getClass() == com.laxcus.access.column.Double.class) {
			if (asc) {
				return Laxkit.compareTo(value, ((com.laxcus.access.column.Double) that).getValue());
			} else {
				return Laxkit.compareTo(((com.laxcus.access.column.Double) that).getValue(), value);
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
	public Float duplicate() {
		return new Float(this);
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
		writer.writeFloat(value);
		return new SHA256Hash(writer.effuse());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (that == null || that.getClass() != com.laxcus.access.column.Float.class) {
			return false;
		} else if (that == this) {
			return true;
		}

		Float column = (Float) that;
		if (!isNull() && !column.isNull()) {
			return value == column.value;
		}
		return column.isNull() == isNull();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (isNull()) return 0;
		return (int) (value);
	}
	
	/*
	 * 返回字符串
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