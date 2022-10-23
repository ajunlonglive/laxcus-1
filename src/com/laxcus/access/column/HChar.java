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
 * 大字符，采用UTF32编码
 * 
 * @author scott.liang
 * @version 1.0 3/9/2009
 * @since laxcus 1.0
 */
public final class HChar extends Word {

	private static final long serialVersionUID = 204518557747117431L;

	/**
	 * 构造一个默认的大字符列
	 */
	public HChar() {
		super(ColumnType.HCHAR);
	}

	/**
	 * 根据传入的大字符列，生成它的副本
	 * @param that HChar实例
	 */
	private HChar(HChar that) {
		super(that);
	}

	/**
	 * 构造一个大字符列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public HChar(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造一个大字符列，并且指定它的列编号和大字符值
	 * @param columnId 列编号
	 * @param value 大字符值
	 */
	public HChar(short columnId, byte[] value) {
		this(columnId);
		setValue(value);
	}

	/**
	 * 构造一个大字符列，并且指定它的列编号、大字符值、索引值
	 * @param columnId 列编号
	 * @param value 大字符值
	 * @param index 索引值
	 */
	public HChar(short columnId, byte[] value, byte[] index){
		this(columnId, value);
		setIndex(index);
	}
	
	/*
	 * 根据当前大字符对象参数，生成一个它的副本
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public HChar duplicate() {
		return new HChar(this);
	}

	/*
	 * 大字符对应UTF32编码
	 * @see com.laxcus.access.column.Word#getCharset()
	 */
	@Override
	public Charset getCharset() {
		return new UTF32();
	}
}