/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

/**
 * 短整形单元
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowShortCell extends ShowIntegralCell {
	
	private static final long serialVersionUID = -1998306206137629752L;

	/** 数值 **/
	private short value;

	/**
	 * 生成短整形单元的数据副本
	 * @param that TableShortCell实例
	 */
	private ShowShortCell(ShowShortCell that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的短整形单元
	 */
	private ShowShortCell() {
		super();
		value = 0;
	}

	/**
	 * 构造短整形单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowShortCell(int index) {
		this();
		setIndex(index);
	}
	
	/**
	 * 构造短整形单元，指定下标和数值
	 * @param index 索引下标
	 * @param value 数值
	 */
	public ShowShortCell(int index, short value) {
		this(index);
		setValue(value);
	}

	/**
	 * 构造短整形单元，指定索引下标、数值、进制数
	 * @param index 索引下标
	 * @param value 数值
	 * @param radix 进制数
	 */
	public ShowShortCell(int index, short value, int radix) {
		this(index);
		setValue(value);
		setRadix(radix);
	}

	/**
	 * 设置数值
	 * @param e short值
	 */
	public void setValue(short e) {
		value = e;
		setNullable(false);
	}
	
	/**
	 * 返回数值
	 * @return short值
	 */
	public short getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#duplicate()
	 */
	@Override
	public ShowShortCell duplicate() {
		return new ShowShortCell(this);
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