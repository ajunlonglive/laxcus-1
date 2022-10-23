/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import com.laxcus.access.type.*;
import com.laxcus.util.charset.*;

/**
 * 宽字符列。字符集采用"UTF16-BE"编码
 * 
 * @author scott.liang
 * @version 1.0 3/9/2009
 * @since laxcus 1.0
 */
public final class WChar extends Word {

	private static final long serialVersionUID = -3268250831317268603L;

	/**
	 * 根据传入的宽字符列，生成一个它的副本
	 * @param that
	 */
	private WChar(WChar that) {
		super(that);
	}

	/**
	 * 构造一个宽字符列
	 */
	public WChar() {
		super(ColumnType.WCHAR);
	}

	/**
	 * 构造宽字符列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public WChar(short columnId) {
		this();
		 setId(columnId);
	}

	/**
	 * 构造宽字符列，并且指定它的列编号和数据值
	 * @param columnId 列编号
	 * @param value 数值
	 */
	public WChar(short columnId, byte[] value) {
		this(columnId);
		 setValue(value);
	}

	/**
	 * 构造宽字符列，并且指定它的列编号、数据值、索引
	 * @param columnId 列编号
	 * @param value 数据值
	 * @param index 索引值
	 */
	public WChar(short columnId, byte[] value, byte[] index) {
		this(columnId, value);
		 setIndex(index);
	}

	/**
	 * 根据当前宽字符列对象实例，生成它的数据副本
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public WChar duplicate() {
		return new WChar(this);
	}

	/**
	 * 宽字符列采用UTF16-BE编码
	 * @see com.laxcus.access.column.Word#getCharset()
	 */
	@Override
	public Charset getCharset() {
		return new UTF16();
	}
}