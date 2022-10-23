/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

/**
 * 整形单元
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowIntegerCell extends ShowIntegralCell {
	
	private static final long serialVersionUID = 6332767470909984298L;

	/** 数值 **/
	private int value;

	/**
	 * 生成整形单元的数据副本
	 * @param that TableIntegerCell实例
	 */
	private ShowIntegerCell(ShowIntegerCell that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的整形单元
	 */
	private ShowIntegerCell() {
		super();
		value = 0;
	}

	/**
	 * 构造整形单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowIntegerCell(int index) {
		this();
		setIndex(index);
	}
	
	/**
	 * 构造整形单元，指定下标和数值
	 * @param index 索引下标
	 * @param value 数值
	 */
	public ShowIntegerCell(int index, int value) {
		this(index);
		setValue(value);
	}

	/**
	 * 构造整形单元，指定索引下标、数值、进制数
	 * @param index 索引下标
	 * @param value 数值
	 * @param radix 进制数
	 */
	public ShowIntegerCell(int index, int value, int radix) {
		this(index);
		setValue(value);
		setRadix(radix);
	}

	/**
	 * 设置数值
	 * @param e int值
	 */
	public void setValue(int e) {
		value = e;
		setNullable(false);
	}
	
	/**
	 * 返回数值
	 * @return int值
	 */
	public int getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#duplicate()
	 */
	@Override
	public ShowIntegerCell duplicate() {
		return new ShowIntegerCell(this);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#visible()
	 */
	@Override
	public Object visible() {
		if (isNullable()) {
			return "null";
		}
		// 判断是16进制数
		if (isHex()) {
			return String.format("0x%x", value);
		}
		return String.format("%d", value);
	}

}