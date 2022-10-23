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
 * 短整型列
 * 
 * @author scott.liang
 * @version 1.0 3/13/2009
 * @since laxcus 1.0
 */
public final class Short extends Number {

	private static final long serialVersionUID = -107927262950354097L;
	
	/** 数值 **/
	private short value;

	/**
	 * 构造一个短整型列
	 */
	public Short() {
		super(ColumnType.SHORT);
	}

	/**
	 * 根据传入的短整型列，生成一个它的副本
	 * @param that Short实例
	 */
	private Short(Short that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造一个短整型列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public Short(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造一个短整型列，并且指定它的列编号和数值
	 * @param columnId 列编号
	 * @param value 数值
	 */
	public Short(short columnId, short value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 设置短整型数值
	 * @param param 数值
	 */
	public void setValue(short param) {
		value = param;
		setNull(false);
	}

	/**
	 * 返回短整型数值
	 * @return short
	 */
	public short getValue() {
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
		setValue(Laxkit.toShort(b, off, len));
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
	 * 固定2个字节
	 * @see com.laxcus.access.column.Number#getNumberSize()
	 */
	@Override
	public int getNumberSize() {
		return 2;
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
		
		if (that.getClass() == com.laxcus.access.column.Short.class) {
			return Laxkit.compareTo(value, ((com.laxcus.access.column.Short) that).value);
		} else if (that.getClass() == com.laxcus.access.column.Integer.class) {
			return Laxkit.compareTo(value, ((com.laxcus.access.column.Integer) that).getValue());
		} else if (that.getClass() == Long.class) {
			return Laxkit.compareTo(value, ((com.laxcus.access.column.Long) that).getValue());
		} else {
			String str = String.format("%s != %s", getClass().getName(), that.getClass().getName());
			throw new ClassCastException(str);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.access.column.Column#compare(com.laxcus.access.column.Column)
	 */
	@Override
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
		
		if (that.getClass() == Short.class) {
			if (asc) {
				return Laxkit.compareTo(value, ((Short) that).value);
			} else {
				return Laxkit.compareTo(((Short) that).value, value);
			}
		} else if (that.getClass() == com.laxcus.access.column.Integer.class) {
			if (asc) {
				return Laxkit.compareTo(value, ((com.laxcus.access.column.Integer) that).getValue());
			} else {
				return Laxkit.compareTo(((com.laxcus.access.column.Integer) that).getValue(), value);
			}
		} else if (that.getClass() == Long.class) {
			if (asc) {
				return Laxkit.compareTo(value, ((com.laxcus.access.column.Long) that).getValue());
			} else {
				return Laxkit.compareTo(((com.laxcus.access.column.Long) that).getValue(), value);
			}
		} else {
			String str = String.format("%s != %s", getClass().getName(), that.getClass().getName());
			throw new ClassCastException(str);
		}
	}
	
	/*
	 * 根据当前短整型列，生成它的副本
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public Column duplicate() {
		return new Short(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.Column#capacity()
	 */
	@Override
	public int capacity() {
		if(isNull()) {
			return 1;
		}
		return 3;
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
		writer.writeShort(value);
		return new SHA256Hash(writer.effuse());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object that) {
		if (that == null || that.getClass() != com.laxcus.access.column.Short.class) {
			return false;
		} else if (that == this) {
			return true;
		}
		
		Short column = (Short) that;
		if (!column.isNull() && !isNull()) {
			return value == column.value;
		}
		return isNull() == column.isNull();
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
	 * 返回字符串
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (isNull()) return null;
		return String.format("%d", value);
	}
}