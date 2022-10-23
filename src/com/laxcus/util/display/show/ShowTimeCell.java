/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

/**
 * 时间单元
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowTimeCell extends ShowItemCell {
	
	private static final long serialVersionUID = -7002424678107192334L;

	/** 时间 **/
	private int value;

	/**
	 * 生成TIME类型单元的数据副本
	 * @param that TableTimeCell实例
	 */
	private ShowTimeCell(ShowTimeCell that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的TIME类型单元
	 */
	private ShowTimeCell() {
		super();
		value = 0;
	}

	/**
	 * 构造TIME类型单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowTimeCell(int index) {
		this();
		setIndex(index);
	}
	
	/**
	 * 构造TIME类型单元，指定索引下标和时间值
	 * @param index 索引下标
	 * @param value 时间值
	 */
	public ShowTimeCell(int index, int value) {
		this(index);
		setValue(value);
	}

	/**
	 * 设置时间值
	 * @param e 时间值
	 */
	public void setValue(int e) {
		value = e;
		setNullable(false);
	}
	
	/**
	 * 返回时间值
	 * @return 时间值
	 */
	public int getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.display.FieldItem#duplicate()
	 */
	@Override
	public ShowTimeCell duplicate() {
		return new ShowTimeCell(this);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.task.display.FieldItem#visible()
	 */
	@Override
	public Object visible() {
		if (isNullable()) {
			return "null";
		}
		return com.laxcus.util.datetime.SimpleTime.format(value);
	}

}