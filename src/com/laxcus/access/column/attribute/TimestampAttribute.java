/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

import com.laxcus.access.type.*;
import com.laxcus.util.classable.*;

/**
 * 时间戳列属性
 * 
 * @author scott.liang
 * @version 1.0 5/12/2009
 * @since laxcus 1.0
 */
public final class TimestampAttribute extends NumberAttribute {

	private static final long serialVersionUID = -3671340348163158473L;

	/** 默认值 **/
	private long value;

	/**
	 * 根据传入参数生成时间戳列属性的副本
	 * @param that TimestampAttribute实例
	 */
	private TimestampAttribute(TimestampAttribute that) {
		super(that);
		value = that.value;
	}
	
	/**
	 * 构造时间戳列属性
	 */
	public TimestampAttribute() {
		super(ColumnType.TIMESTAMP);
		value = 0L;
	}

	/**
	 * 构造时间戳列属性，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public TimestampAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造时间戳列属性，并且指定它的列编号和名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public TimestampAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 构造时间戳列属性，并且指定它的列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public TimestampAttribute(short columnId, String title, long value) {
		this(columnId, title);
		setValue(value);
	}

	/**
	 * 设置时间戳列属性的默认值
	 * @param param 时间戳列长整型值
	 * @return 设置成功返回真，否则假
	 */
	public boolean setValue(long param) {
		if(!isSetStatus()) {
			return false;
		}
		value = param;
		setNull(false);
		return true;
	}

	/**
	 * 返回时间戳列属性的默认值
	 * @return 时间戳列长整型值
	 */
	public long getValue() {
		return value;
	}

	/*
	 * 生成一个时间戳列的默认值
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault(short)
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.Timestamp column = new com.laxcus.access.column.Timestamp(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.Timestamp) getFunction().getDefault();
			column.setId(columnId);
		} else if (value != 0L) {
			column.setValue(value);
		}
		return column;
	}

	/**
	 * 将时间戳列属性参数输出到可类化存储器
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(value);
	}

	/**
	 * 从可类化读取器中解析时间戳列属性参数
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		value = reader.readLong();
	}

	/**
	 * 克隆时间戳列属性
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public TimestampAttribute duplicate() {
		return new TimestampAttribute(this);
	}
}