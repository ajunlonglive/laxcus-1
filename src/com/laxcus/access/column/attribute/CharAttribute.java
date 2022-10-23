/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

import com.laxcus.access.type.*;
import com.laxcus.util.charset.*;

/**
 * 字符列(UTF8编码)属性
 * 
 * @author scott.liang
 * @version 1.0 4/27/2009
 * @since laxcus 1.0
 */
public final class CharAttribute extends WordAttribute {

	private static final long serialVersionUID = 4632511659001756126L;

	/**
	 * 根据传入参数建立字符列属性的副本
	 * @param that
	 */
	private CharAttribute(CharAttribute that) {
		super(that);
	}

	/**
	 * 构造一个默认的字符列属性
	 */
	public CharAttribute() {
		super(ColumnType.CHAR);
	}

	/**
	 * 建立字符列属性，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public CharAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 建立字符列属性，并且指定它的列编号和名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public CharAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 建立字符列属性，并且指定它的列编号、名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public CharAttribute(short columnId, String title, byte[] value) {
		this(columnId, title);
		setValue(value);
	}
	
	/*
	 * 生成一个默认列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault(short)
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.Char column = new com.laxcus.access.column.Char(columnId);
		
		if (getFunction() != null) {
			column = (com.laxcus.access.column.Char) getFunction().getDefault();
			column.setId(columnId);
		} else if (value != null || index != null) {
			column.setValue(value);
			column.setIndex(index);
			column.addRWords(likeArray);
		}
		return column;
	}

	/**
	 * 生成当前字符列的副本
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public ColumnAttribute duplicate() {
		return new CharAttribute(this);
	}

	/**
	 * 单字符返回UTF8编码的字符集
	 * @see com.laxcus.access.column.attribute.WordAttribute#getCharset()
	 */
	@Override
	public Charset getCharset() {
		return new UTF8();
	}
}