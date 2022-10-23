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
 * 日期列属性
 * 
 * @author scott.liang
 * @version 1.0 5/2/2009
 * @since laxcus 1.0
 */
public final class DateAttribute extends NumberAttribute {
	
	private static final long serialVersionUID = 5281282045199325760L;

	/** 时间默认值 **/
	private int value;

	/**
	 * 根据传入参数构造日期列属性的副本
	 * @param that DateAttribute实例
	 */
	private DateAttribute(DateAttribute that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的日期列属性
	 */
	public DateAttribute() {
		super(ColumnType.DATE);
		value = 0;
	}

	/**
	 * 构造日期列属性，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public DateAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造日期列属性，并且指定它的列编号和名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public DateAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 构造日期列属性，并且指定它的列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public DateAttribute(short columnId, String title, int value) {
		this(columnId, title);
		setValue(value);
	}
	
	/**
	 * 设置默认的日期值
	 * @param param 日期值
	 * @return 设置成功返回真，否则假
	 */
	public boolean setValue(int param) {
		if (!isSetStatus()) {
			return false;
		}
		value = param;
		setNull(false);
		return true;
	}

	/**
	 * 返回默认日期值
	 * @return 日期整型值
	 */
	public int getValue() {
		return value;
	}

	/*
	 * 生产一个默认日期列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault()
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.Date column = new com.laxcus.access.column.Date(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.Date) getFunction().getDefault();
			column.setId(columnId);
		} else if (value != 0) {
			column.setValue(value);
		}
		return column;
	}

	/**
	 * 生成日期列属性的数据流到可类化存储器
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(value);
	}

	/**
	 * 从可类化读取器中解析日期列属性
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		value = reader.readInt();
	}

	/*
	 * 克隆日期列属性
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public ColumnAttribute duplicate() {
		return new DateAttribute(this);
	}
}