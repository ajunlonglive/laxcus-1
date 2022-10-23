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
 * 大字符(UTF32编码)列属性
 * 
 * @author scott.liang
 * @version 1.0 5/3/2009
 * @since laxcus 1.0
 */
public final class HCharAttribute extends WordAttribute {

	private static final long serialVersionUID = -4739550225934801235L;

	/**
	 * 根据传入的HCharAttribute实例生成它的副本
	 * @param that HCharAttribute实例
	 */
	private HCharAttribute(HCharAttribute that) {
		super(that);
	}

	/**
	 * 生成一个默认的大字符列属性
	 */
	public HCharAttribute() {
		super(ColumnType.HCHAR);
	}
	
	/**
	 * 生成一个大字符列属性，并且指定的列编号
	 * @param columnId 列编号
	 */
	public HCharAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 生成一个大字符列属性，并且指定的列编号、列名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public HCharAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 生成一个大字符列属性，并且指定的列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public HCharAttribute(short columnId, String title, byte[] value) {
		this(columnId, title);
		setValue(value);
	}
	
	/*
	 * 生成大字符列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault()
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.HChar column = new com.laxcus.access.column.HChar(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.HChar) getFunction().getDefault();
			column.setId(columnId);
		} else if (value != null || index != null) {
			column.setValue(value);
			column.setIndex(index);
			column.addRWords(likeArray);
		}
		return column;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#duplicate()
	 */
	@Override
	public HCharAttribute duplicate() {
		return new HCharAttribute(this);
	}
	
	/*
	 * 大字符返回UTF32编码的字符集
	 * @see com.laxcus.access.column.attribute.WordAttribute#getCharset()
	 */
	@Override
	public Charset getCharset() {
		return new UTF32();
	}
}