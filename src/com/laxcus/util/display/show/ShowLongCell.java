/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

/**
 * 长整型单元
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowLongCell extends ShowIntegralCell {
	
	private static final long serialVersionUID = 3477561339038750298L;

	/** 数值 **/
	private long value;

	/**
	 * 生成长整型单元的数据副本
	 * @param that TableLongCell实例
	 */
	private ShowLongCell(ShowLongCell that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的长整型单元
	 */
	private ShowLongCell() {
		super();
		value = 0;
	}

	/**
	 * 构造长整型单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowLongCell(int index) {
		this();
		setIndex(index);
	}
	
	/**
	 * 构造长整型单元，指定下标和数值
	 * @param index 索引下标
	 * @param value 数值
	 */
	public ShowLongCell(int index, long value) {
		this(index);
		setValue(value);
	}

	/**
	 * 构造长整形单元，指定索引下标、数值、进制数
	 * @param index 索引下标
	 * @param value 数值
	 * @param radix 进制数
	 */
	public ShowLongCell(int index, long value, int radix) {
		this(index);
		setValue(value);
		setRadix(radix);
	}

	/**
	 * 设置数值
	 * @param e long值
	 */
	public void setValue(long e) {
		value = e;
		setNullable(false);
	}
	
	/**
	 * 返回数值
	 * @return long值
	 */
	public long getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#duplicate()
	 */
	@Override
	public ShowLongCell duplicate() {
		return new ShowLongCell(this);
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