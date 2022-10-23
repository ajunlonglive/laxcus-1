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
 * 宽字符(UTF16编码)列属性
 * 
 * @author scott.liang
 * @version 1.0 5/5/2009
 * @since laxcus 1.0
 */
public final class WCharAttribute extends WordAttribute {

	private static final long serialVersionUID = -1807825565317335597L;

	/**
	 * 根据传入参数生成宽字符列属性的副本
	 * @param that WCharAttribute实例
	 */
	private WCharAttribute(WCharAttribute that) {
		super(that);
	}

	/**
	 * 构造默认的宽字符列属性
	 */
	public WCharAttribute() {
		super(ColumnType.WCHAR);
	}

	/**
	 * 生成宽字符列属性，并且指定列编号
	 * @param columnId 列编号
	 */
	public WCharAttribute(short columnId) {
		this();
		setColumnId(columnId);
	}

	/**
	 * 生成宽字符列属性，并且指定列编号、列名称
	 * @param columnId 列编号
	 * @param title 列标题
	 */
	public WCharAttribute(short columnId, String title) {
		this(columnId);
		setName(title);
	}

	/**
	 * 生成宽字符列属性，并且指定列编号、列名称、默认值
	 * @param columnId 列编号
	 * @param title 列标题
	 * @param value 列数值
	 */
	public WCharAttribute(short columnId, String title, byte[] value) {
		this(columnId, title);
		setValue(value);
	}

	/**
	 * 生成宽字符列
	 * @see com.laxcus.access.column.attribute.ColumnAttribute#getDefault(short)
	 */
	@Override
	public com.laxcus.access.column.Column getDefault() {
		short columnId = getColumnId();
		com.laxcus.access.column.WChar column = new com.laxcus.access.column.WChar(columnId);

		if (getFunction() != null) {
			column = (com.laxcus.access.column.WChar) getFunction().getDefault();
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
	public WCharAttribute duplicate() {
		return new WCharAttribute(this);
	}

	/**
	 * 宽字符返回UTF16编码的字符集
	 * @see com.laxcus.access.column.attribute.WordAttribute#getCharset()
	 */
	@Override
	public Charset getCharset() {
		return new UTF16();
	}
}