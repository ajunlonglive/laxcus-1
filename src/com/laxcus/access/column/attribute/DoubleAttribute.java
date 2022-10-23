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
 * 双浮点列属性
 * 
 * @author scott.liang
 * @version 1.0 4/28/2009
 * @since laxcus 1.0
 */
public final class DoubleAttribute extends NumberAttribute {
	
	private static final long serialVersionUID = -8182813171459862734L;

	/** 默认值 **/
	private double value;
	
	/**
	 * 根据传入参数生成双浮点列属性的副本
	 * @param that DoubleAttribute实例
	 */
	private DoubleAttribute(DoubleAttribute that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的双浮点列属性
	 */
	public DoubleAttribute() {
		super(ColumnType.DOUBLE);
		value = 0.0f;
	}

	/**
	 * 构造双浮点列属性，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public DoubleAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造双浮点列属性，并且指定它的列编号和名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public DoubleAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 构造双浮点列属性，并且指定它的列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public DoubleAttribute(short columnId, String title, double value) {
		this(columnId, title);
		setValue(value);
	}

	/**
	 * 设置一个默认值
	 * @param param 双浮点值
	 * @return 设置成功返回真，否则假
	 */
	public boolean setValue(double param) {
		if (!isSetStatus()) {
			return false;
		}
		value = param;
		setNull(false);
		return true;
	}

	/**
	 * 返回双浮点默认值
	 * @return double
	 */
	public double getValue() {
		return value;
	}

	/*
	 * 生成双浮点列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault()
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.Double column = new com.laxcus.access.column.Double(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.Double) getFunction().getDefault();
			column.setId(columnId);
		} else if (value != 0.0f) {
			column.setValue(value);
		}
		return column;
	}

	/**
	 * 双浮点列属性参数输出到可类化存储器
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeDouble(value);
	}

	/**
	 * 从可类化读取器中解析双浮点列属性
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		value = reader.readDouble();
	}
	
	/**
	 * 克隆双浮点列属性
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public ColumnAttribute duplicate() {
		return new DoubleAttribute(this);
	}

}