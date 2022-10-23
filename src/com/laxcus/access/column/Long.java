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
 * 长整型列(bit64)
 * 
 * @author scott.liang
 * @version 1.0 3/13/2009
 * @since laxcus 1.0
 */
public final class Long extends Number {

	private static final long serialVersionUID = 7050915405036361988L;

	/** 长整型数值 **/
	private long value;

	/**
	 * 构造一个长整型列
	 */
	public Long() {
		super(ColumnType.LONG);
	}
	
	/**
	 * 根据传入的长整型列，生成它的副本
	 * @param that Long实例
	 */
	private Long(Long that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造一个长整型列，并且指定列编号
	 * @param columnId 列编号
	 */
	public Long(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造一个长整型列，并且指定列编号和数值
	 * @param columnId 列编号
	 * @param value 数值
	 */
	public Long(short columnId, long value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 设置长整型列数值
	 * @param param 列数值
	 */
	public void setValue(long param) {
		value = param;
		setNull(false);
	}

	/**
	 * 返回长整型列数值
	 * @return 长整值
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
	 * 长整型固定8个字节
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
		
		if (that.getClass() == com.laxcus.access.column.Long.class) {
			return Laxkit.compareTo(value, ((com.laxcus.access.column.Long) that).value);
		} else if (that.getClass() == com.laxcus.access.column.Short.class) {
			return Laxkit.compareTo(value, ((com.laxcus.access.column.Short) that).getValue());
		} else if (that.getClass() == com.laxcus.access.column.Integer.class) {
			return Laxkit.compareTo(value, ((com.laxcus.access.column.Integer) that).getValue());
		} else {
			String str = String.format("%s != %s", getClass().getName(), that.getClass().getName());
			throw new ClassCastException(str);
		}
	}

	public int compare(Column that, boolean asc) {
		if (that == null) {
			return 1;
		} else if (isNull() && that.isNull()) {
			return 0;
		} else if (isNull()) {
			return -1;
		} else if (that.isNull()) {
			return 1;
		}
		
		if (that.getClass() == com.laxcus.access.column.Long.class) {
			if(asc) {
				return Laxkit.compareTo(value, ((com.laxcus.access.column.Long) that).value);
			} else {
				return Laxkit.compareTo( ((com.laxcus.access.column.Long) that).value, value);
			}
		} else if (that.getClass() == com.laxcus.access.column.Short.class) {
			if(asc) {
				return Laxkit.compareTo(value, ((com.laxcus.access.column.Short) that).getValue());
			} else {
				return Laxkit.compareTo( ((com.laxcus.access.column.Short) that).getValue(), value);
			}
		} else if (that.getClass() == com.laxcus.access.column.Integer.class) {
			if(asc) {
				return Laxkit.compareTo(value, ((com.laxcus.access.column.Integer) that).getValue());
			} else {
				return Laxkit.compareTo( ((com.laxcus.access.column.Integer) that).getValue(), value);
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
	public Long duplicate() {
		return new Long(this);
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
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object that) {
		if (that == null || that.getClass() != com.laxcus.access.column.Long.class) {
			return false;
		} else if(that == this) {
			return true;
		}

		Long s = (Long) that;
		if (!s.isNull() && !isNull()) {
			return value == s.value;
		}
		return s.isNull() == isNull();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if(isNull()) return 0;
		return (int) (value >>> 32 ^ value);
	}

	/*
	 * 返回字符串
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isNull()) return null;
		return String.format("%d", value);
	}
}