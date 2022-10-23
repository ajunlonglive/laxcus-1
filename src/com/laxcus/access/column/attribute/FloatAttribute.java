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
 * 单浮点列属性
 * 
 * @author scott.liang
 * @version 1.0 5/5/2009
 * @since laxcus 1.0
 */
public final class FloatAttribute extends NumberAttribute {

	private static final long serialVersionUID = 8316769408372858692L;

	/** 默认值 **/
	private float value;

	/**
	 * 根据传入参数构造单浮点列属性的副本
	 * @param that FloatAttribute实例
	 */
	private FloatAttribute(FloatAttribute that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造单浮点列属性
	 */
	public FloatAttribute() {
		super(ColumnType.FLOAT);
		value = 0.0f;
	}

	/**
	 * 构造单浮点列属性，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public FloatAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造单浮点列属性，并且指定它的列编号和名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public FloatAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 构造单浮点列属性，并且指定它的列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public FloatAttribute(short columnId, String title, float value) {
		this(columnId, title);
		setValue(value);
	}

	/**
	 * 设置单浮点默认值
	 * @param param 单浮点值
	 * @return 设置成功返回真，否则假
	 */
	public boolean setValue(float param) {
		if(!isSetStatus()) {
			return false;
		}
		value = param;
		setNull(false);
		return true;
	}

	/**
	 * 返回单浮点默认值
	 * @return float
	 */
	public float getValue() {
		return value;
	}

	/*
	 * 生成一个默认单浮点值
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault()
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.Float column = new com.laxcus.access.column.Float(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.Float)getFunction().getDefault();
			column.setId(columnId);
		} else if (value != 0.0f) {
			column.setValue(value);
		}
		return column;
	}

	/**
	 * 将单浮点列属性参数输出到可类化存储器
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeFloat(value);
	}

	/**
	 * 从可类化读取器中解析单浮点参数
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		value = reader.readFloat();
	}
	
	/*
	 * 克隆当前单浮点列属性
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public ColumnAttribute duplicate() {
		return new FloatAttribute(this);
	}
}