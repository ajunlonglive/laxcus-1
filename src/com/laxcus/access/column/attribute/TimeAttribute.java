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
 * 时间列属性
 * 
 * @author scott.liang
 * @version 1.0 5/9/2009
 * @since laxcus 1.0
 */
public final class TimeAttribute extends NumberAttribute {

	private static final long serialVersionUID = -6041524691076271048L;

	/** 默认值 **/
	private int value;

	/**
	 * 根据传入参数生成时间列属性的副本
	 * @param that TimeAttribute实例
	 */
	private TimeAttribute(TimeAttribute that) {
		super(that);
		value = that.value;
	}
	
	/**
	 * 构造默认的时间列属性
	 */
	public TimeAttribute() {
		super(ColumnType.TIME);
		value = 0;
	}

	/**
	 * 构造时间列属性，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public TimeAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造时间列属性，并且指定它的列编号和名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public TimeAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 构造时间列属性，并且指定它的列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public TimeAttribute(short columnId, String title, int value) {
		this(columnId, title);
		setValue(value);
	}

	/**
	 * 设置时间列属性的默认值
	 * 
	 * @param param 时间列整型值
	 * @return 设置成功返回真，否则假
	 */
	public boolean setValue(int param) {
		if(!isSetStatus()) {
			return false;
		}
		value = param;
		setNull(false);
		return true;
	}

	/**
	 * 返回时间列属性的默认值
	 * @return 时间列整型值
	 */
	public int getValue() {
		return value;
	}

	/*
	 * 生成一个时间列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault()
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.Time column = new com.laxcus.access.column.Time(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.Time) getFunction().getDefault();
			column.setId(columnId);
		} else if (value != 0) {
			column.setValue(value);
		}
		return column;
	}

	/**
	 * 将时间列属性参数输出到可类化存储器
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(value);
	}

	/**
	 * 从可类化读取器中解析时间列属性参数
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		value = reader.readInt();
	}

	/**
	 * 用当前时间列属性，生成一个它的数据副本
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public TimeAttribute duplicate() {
		return new TimeAttribute(this);
	}
}