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
 * 短整型列(BIT16)属性
 * 
 * @author scott.liang
 * @version 1.0 5/4/2009
 * @since laxcus 1.0
 */
public final class ShortAttribute extends NumberAttribute {
	
	private static final long serialVersionUID = 5265615993324758486L;
	
	/** 默认值 **/
	private short value;

	/**
	 * 根据传入参数生成短整型列属性的副本
	 * @param that ShortAttribute实例
	 */
	private ShortAttribute(ShortAttribute that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造短整型列属性
	 */
	public ShortAttribute() {
		super(ColumnType.SHORT);
		value = 0;
	}

	/**
	 * 构造短整型列属性，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public ShortAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造短整型列属性，并且指定它的列编号和名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public ShortAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 构造短整型列属性，并且指定它的列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public ShortAttribute(short columnId, String title, short value) {
		this(columnId, title);
		setValue(value);
	}

	/**
	 * 设置的短整型默认值
	 * @param param 短整值
	 * @return 设置成功返回真，否则假
	 */
	public boolean setValue(short param) {
		if (!isSetStatus()) {
			return false;
		}
		value = param;
		setNull(false);
		return true;
	}

	/**
	 * 返回默认值
	 * @return 短整型
	 */
	public short getValue() {
		return value;
	}

	/*
	 * 建立默认的短整型列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault()
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
			short columnId = getColumnId();
		com.laxcus.access.column.Short column = new com.laxcus.access.column.Short(columnId);
		
		
		if (getFunction() != null) {
			column = (com.laxcus.access.column.Short) getFunction().getDefault();
			column.setId(columnId);
		} else if (value != 0) {
			column.setValue(value);
		}
		return column;
	}
	
	/**
	 * 将短整型值写入可类化存储器
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeShort(value);
	}

	/**
	 * 从可类化读取器中读取短整型值
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		value = reader.readShort();
	}
	
	/**
	 * 克隆当前短整型列属性
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public ShortAttribute duplicate() {
		return new ShortAttribute(this);
	}
}