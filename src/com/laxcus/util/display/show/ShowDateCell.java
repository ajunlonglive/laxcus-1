/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

/**
 * 日期单元
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowDateCell extends ShowItemCell {
	
	private static final long serialVersionUID = -6416560336471647534L;

	/** 数值 **/
	private int value;

	/**
	 * 生成DATE类型单元的数据副本
	 * @param that TableDateCell实例
	 */
	private ShowDateCell(ShowDateCell that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的DATE类型单元
	 */
	private ShowDateCell() {
		super();
		value = 0;
	}

	/**
	 * 构造DATE类型单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowDateCell(int index) {
		this();
		setIndex(index);
	}
	
	/**
	 * 构造DATE类型单元，指定下标和数值
	 * @param index 索引下标
	 * @param value 数值
	 */
	public ShowDateCell(int index, int value) {
		this(index);
		setValue(value);
	}

	/**
	 * 设置数值
	 * @param e 整型值
	 */
	public void setValue(int e) {
		value = e;
		setNullable(false);
	}
	
	/**
	 * 返回数值
	 * @return 整型值
	 */
	public int getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#duplicate()
	 */
	@Override
	public ShowDateCell duplicate() {
		return new ShowDateCell(this);
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
		return com.laxcus.util.datetime.SimpleDate.format(value);
	}

}