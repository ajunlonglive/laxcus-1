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
 * 基于单字符的模糊检索关键字列。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/19/2009
 * @since laxcus 1.0
 */
public final class RChar extends RWord {

	private static final long serialVersionUID = 5152111807794621809L;

	/**
	 * 构造一个单字符模糊检索关键字列
	 */
	public RChar() {
		super(ColumnType.RCHAR);
	}

	/**
	 * 根据传入的模糊检索关键字列，生成一个它的副本
	 * @param that RChar实例
	 */
	private RChar(RChar that) {
		super(that);
	}

	/**
	 * 构造一个模糊检索关键字列，并且指定它的列编号
	 * @param columnId 列编号
	 */
	public RChar(short columnId) {
		this();
		setId(columnId);
	}

	/**
	 * 构造一个模糊检索关键字列，并且指定它的列编号和关键字值
	 * @param columnId 列编号
	 * @param index 索引值
	 */
	public RChar(short columnId, byte[] index) {
		this(columnId);
		setIndex(index);
	}

	/**
	 * 构造一个模糊检索关键字列，并且指定它的列编号、忽略范围、关键字值
	 * @param columnId 列编号
	 * @param left 左侧值
	 * @param right 右侧值
	 * @param index 索引值
	 */
	public RChar(short columnId, short left, short right, byte[] index) {
		this(columnId, index);
		setRange(left, right);
	}

	/**
	 * 根据当前模糊检索关键字列，生成它的副本
	 * @see com.laxcus.access.column.Column#duplicate()
	 */
	@Override
	public RChar duplicate() {
		return new RChar(this);
	}

}