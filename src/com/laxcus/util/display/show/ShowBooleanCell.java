/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

/**
 * 布尔单元
 * 
 * @author scott.liang
 * @version 1.0 7/17/2021
 * @since laxcus 1.0
 */
public class ShowBooleanCell extends ShowItemCell {
	
	private static final long serialVersionUID = 1231092984872899250L;
	
	/** 时间 **/
	private boolean value;

	/**
	 * 生成T布尔类型单元的数据副本
	 * @param that TableBooleanCell实例
	 */
	private ShowBooleanCell(ShowBooleanCell that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的T布尔类型单元
	 */
	private ShowBooleanCell() {
		super();
		value = false;
	}

	/**
	 * 构造T布尔类型单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowBooleanCell(int index) {
		this();
		setIndex(index);
	}
	
	/**
	 * 构造T布尔类型单元，指定索引下标和布尔值
	 * @param index 索引下标
	 * @param value 布尔值
	 */
	public ShowBooleanCell(int index, boolean value) {
		this(index);
		setValue(value);
	}

	/**
	 * 设置布尔值
	 * @param b 布尔值
	 */
	public void setValue(boolean b) {
		value = b;
		setNullable(false);
	}
	
	/**
	 * 返回布尔值
	 * @return 布尔值
	 */
	public boolean getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#duplicate()
	 */
	@Override
	public ShowBooleanCell duplicate() {
		return new ShowBooleanCell(this);
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
		return (value ? "true" : "false");
	}

	public String toString() {
		if (isNullable()) {
			return "null";
		}
		return (value ? "true" : "false");
	}
}