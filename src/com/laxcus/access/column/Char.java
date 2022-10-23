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
 * 单字符列。<br>
 * 默认采用UTF8编码。
 * 
 * @author scott.liang
 * @version 1.0 3/9/2009
 * @since laxcus 1.0
 */
public final class Char extends Word {

	private static final long serialVersionUID = -6875035240942644066L;

	/**
	 * 构造默认的单字符列
	 */
	public Char() {
		super(ColumnType.CHAR);
	}

	/**
	 * 根据传入参数构造字符列的副本
	 * 
	 * @param that Char实例
	 */
	private Char(Char that) {
		super(that);
	}

	/**
	 * 构造字符列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public Char(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造字符列，并且指定它的列编号和字符值
	 * @param columnId 列编号
	 * @param value 字符值
	 */
	public Char(short columnId, byte[] value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 构造字符列，并且指定它的列编号、字符值、索引值
	 * @param columnId 列编号
	 * @param value 字符值
	 * @param index 索引值
	 */
	public Char(short columnId, byte[] value, byte[] index) {
		this(columnId, value);
		setIndex(index);
	}

	/**
	 * 生成当前字符列的数据副本
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public Char duplicate() {
		return new Char(this);
	}

	/**
	 * 单字符对应UTF8编码
	 * @see com.laxcus.access.column.Word#getCharset()
	 */
	@Override
	public Charset getCharset() {
		return new UTF8();
	}
	

//	public static void main(String[] args) {
//		Char de = new Char((short)1, new byte[0]);
//		byte[] b = de.build();
//		System.out.printf("build length:%d\n", b.length);
//		
//		int len = de.resolve(b, 0, b.length);
//		System.out.printf("resolve length:%d\n", len);
//	}
}