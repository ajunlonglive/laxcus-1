/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

/**
 * 时间戳单元
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowTimestampCell extends ShowItemCell {
	
	private static final long serialVersionUID = -7641674034960635835L;
	
	/** 时间戳值 **/
	private long value;

	/**
	 * 生成时间戳单元的数据副本
	 * @param that TableTimestampCell实例
	 */
	private ShowTimestampCell(ShowTimestampCell that) {
		super(that);
		value = that.value;
	}

	/**
	 * 构造默认的时间戳单元
	 */
	private ShowTimestampCell() {
		super();
		value = 0;
	}

	/**
	 * 构造时间戳单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowTimestampCell(int index) {
		this();
		setIndex(index);
	}
	
	/**
	 * 构造时间戳单元，指定索引下标和时间戳值
	 * @param index 索引下标
	 * @param value 时间戳值
	 */
	public ShowTimestampCell(int index, long value) {
		this(index);
		setValue(value);
	}

	/**
	 * 设置时间戳值
	 * @param e
	 */
	public void setValue(long e) {
		value = e;
		setNullable(false);
	}
	
	/**
	 * 返回时间戳值
	 * @return 时间戳值
	 */
	public long getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#duplicate()
	 */
	@Override
	public ShowItemCell duplicate() {
		return new ShowTimestampCell(this);
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
		return com.laxcus.util.datetime.SimpleTimestamp.format(value);
	}

}