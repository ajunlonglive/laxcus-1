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
 * 长整型列(BIT64)属性。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/5/2009
 * @since laxcus 1.0
 */
public final class LongAttribute extends NumberAttribute {
	
	private static final long serialVersionUID = 8364034515255768990L;
	
	/** 默认值 **/
	private long value;

	/**
	 * 根据传入参数构造长整型列属性的副本
	 * @param that LongAttribute实例
	 */
	private LongAttribute(LongAttribute that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的长整型列属性
	 */
	public LongAttribute() {
		super(ColumnType.LONG);
		value = 0L;
	}

	/**
	 * 构造长整型列属性，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public LongAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 构造长整型列属性，并且指定它的列编号和名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public LongAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 构造长整型列属性，并且指定它的列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public LongAttribute(short columnId, String title, long value) {
		this(columnId, title);
		setValue(value);
	}

	/**
	 * 设置长整型默认值
	 * @param param 长整型值
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
	 * 返回长整型默认值
	 * @return 长整型值
	 */
	public long getValue() {
		return value;
	}

	/*
	 * 生成一个长整型列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault()
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.Long column = new com.laxcus.access.column.Long(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.Long)getFunction().getDefault();
			column.setId(columnId);
		} else if (value != 0L) {
			column.setValue(value);
		}
		return column;
	}

	/**
	 * 将当前长整型列属性输出到可类化存储器
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#buildSuffix(com.laxcus.util.classable.ClassWriter)
	 */
	@Override
	protected void buildSuffix(ClassWriter writer) {
		writer.writeLong(value);
	}

	/**
	 * 从可类化读取器中解析长整型列属性
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#resolveSuffix(com.laxcus.util.classable.ClassReader)
	 */
	@Override
	protected void resolveSuffix(ClassReader reader) {
		value = reader.readLong();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public LongAttribute duplicate() {
		return new LongAttribute(this);
	}
}