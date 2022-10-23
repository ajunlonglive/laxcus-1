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
 * 基于大字符(4字节)的模糊检索关键字列。<br>
 * 
 * @author scott.liang
 * @version 3/19/2009
 * @since laxcus 1.0
 */
public final class RHChar extends RWord {

	private static final long serialVersionUID = 3062133267230216253L;

	/**
	 * 构造大字符模糊检索关键字列
	 */
	public RHChar() {
		super(ColumnType.RHCHAR);
	}
	
	/**
	 * 根据传入的大字符模糊检索列，生成它的副本
	 * @param that RHChar实例
	 */
	private RHChar(RHChar that) {
		super(that);
	}

	/**
	 * 构造大字符模糊检索关键字列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public RHChar(short columnId) {
		this();
		super.setId(columnId);
	}

	/**
	 * 构造大字符模糊检索关键字列，并且指定它的列编号和关键字值
	 * @param columnId 列编号
	 * @param index 索引值
	 */
	public RHChar(short columnId, byte[] index) {
		this(columnId);
		this.setIndex(index);
	}

	/**
	 * 构造大字符模糊检索关键字列，并且指定它的列编号、忽略范围、关键字值
	 * @param columnId 列编号
	 * @param left 左侧值
	 * @param right 右侧值
	 * @param index 索引值
	 */
	public RHChar(short columnId, short left, short right, byte[] index) {
		this(columnId, index);
		super.setRange(left, right);
	}

	/*
	 * 根据当前大字符模糊检索列，生成它的副本
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public RHChar duplicate() {
		return new RHChar(this);
	}

}