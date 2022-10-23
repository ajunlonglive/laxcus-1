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
 * 整型列(bit32)属性
 * 
 * @author scott.liang
 * @version 1.0 5/6/2009
 * @since laxcus 1.0
 */
public final class IntegerAttribute extends NumberAttribute {

	private static final long serialVersionUID = 2651248947792090784L;

	/** 默认整型值 **/
	private int value;

	/**
	 * 根据传入参数建立整型值列属性的副本
	 * @param that IntegerAttribute实例
	 */
	private IntegerAttribute(IntegerAttribute that) {
		super(that);
		value = that.value;
	}
	
	/**
	 * 构造默认的整型列属性
	 */
	public IntegerAttribute() {
		super(ColumnType.INTEGER);
		value = 0;
	}

	/**
	 * 构造整型列属性，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public IntegerAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造整型列属性，并且指定它的列编号和名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public IntegerAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 构造整型列属性，并且指定它的列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public IntegerAttribute(short columnId, String title, int value) {
		this(columnId, title);
		setValue(value);
	}

	/**
	 * 设置整型默认值
	 * @param param 整型值
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
	 * 返回整型默认值
	 * @return int
	 */
	public int getValue() {
		return value;
	}

	/**
	 * 返回默认的整型列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault()
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.Integer column = new com.laxcus.access.column.Integer(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.Integer) getFunction().getDefault();
			column.setId(columnId);
		} else if (value != 0) {
			column.setValue(value);
		}
		return column;
	}

	/**
	 * 将整型列属性参数输出到可类化存储器
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeInt(value);
	}

	/**
	 * 从可类化读取器中解析整型列属性参数
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		value = reader.readInt();
	}
	
	/**
	 * 克隆当前整型列属性
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public IntegerAttribute duplicate() {
		return new IntegerAttribute(this);
	}
}