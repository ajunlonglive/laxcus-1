/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com, All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import com.laxcus.access.type.*;

/**
 * 基于宽字符(UTF16双字节)的模糊检索关键字列
 * 
 * @author scott.liang
 * @version 1.0 3/19/2009
 * @since laxcus 1.0
 */
public final class RWChar extends RWord {

	private static final long serialVersionUID = 3539686365540331197L;

	/**
	 * 根据传入的宽字符检索列，生成它的副本
	 * @param that RWChar实例
	 */
	private RWChar(RWChar that) {
		super(that);
	}

	/**
	 * 构造宽字符模糊检索关键字列
	 */
	public RWChar() {
		super(ColumnType.RWCHAR);
	}

	/**
	 * 构造宽字符模糊检索关键字列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public RWChar(short columnId) {
		this();
		super.setId(columnId);
	}

	/**
	 * 构造宽字符模糊检索关键字列，并且指定它的列编号和检索关键字
	 * @param columnId 列编号
	 * @param index 索引值
	 */
	public RWChar(short columnId, byte[] index) {
		this(columnId);
		setIndex(index);
	}

	/**
	 * 构造宽字符模糊检索关键字列，并且指定它的列编号、忽略范围、检索关键字
	 * @param columnId 列编号
	 * @param left 左侧值
	 * @param right 右侧值
	 * @param index 索引值
	 */
	public RWChar(short columnId, short left, short right, byte[] index) {
		this(columnId, index);
		super.setRange(left, right);
	}

	/*
	 * 根据当前宽字符检索关键字列，生成它的副本
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public RWChar duplicate() {
		return new RWChar(this);
	}
	
}