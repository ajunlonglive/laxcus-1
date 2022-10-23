/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.display.show;

import com.laxcus.util.*;

/**
 * 双浮点单元
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowDoubleCell extends ShowItemCell {

	private static final long serialVersionUID = 5930267853583453720L;

	/** 数值 **/
	private double value;

	/** 尾数精度 **/
	private int tail;
	
	/** 科学计算数法 **/
	private boolean scientific;
	
	/**
	 * 设置为科学计算数法显示
	 * @param b 真或者假
	 */
	public void setScientific(boolean b) {
		scientific = b;
	}

	/**
	 * 判断是科学计数法显示
	 * @return 真或者假
	 */
	public boolean isScientific() {
		return scientific;
	}

	/**
	 * 生成双浮点单元的数据副本
	 * @param that TableDoubleCell实例
	 */
	private ShowDoubleCell(ShowDoubleCell that) {
		super(that);
		value = that.value;
		tail = that.tail;
		scientific = that.scientific;
	}

	/**
	 * 构造默认的双浮点单元
	 */
	private ShowDoubleCell() {
		super();
		value = 0.0f;
		tail = 0;
		scientific = false;
	}

	/**
	 * 构造双浮点单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowDoubleCell(int index) {
		this();
		setIndex(index);
	}

	/**
	 * 构造双浮点单元，指定下标和数值
	 * @param index 下标
	 * @param value 数值
	 */
	public ShowDoubleCell(int index, double value) {
		this(index);
		setValue(value);
	}

	/**
	 * 构造双浮点单元，指定下标、数值、尾数精度
	 * @param index 下标
	 * @param value 数值
	 * @param tail 尾数精度
	 */
	public ShowDoubleCell(int index, double value, int tail) {
		this(index, value);
		setTail(tail);
	}

	/**
	 * 设置数值
	 * @param e double值
	 */
	public void setValue(double e) {
		value = e;
		setNullable(false);
	}

	/**
	 * 返回数值
	 * @return double值
	 */
	public double getValue() {
		return value;
	}

	/**
	 * 设置尾数精度
	 * @param i int值
	 */
	public void setTail(int i) {
		if (i < 0) {
			throw new IllegalValueException("illegal tail %d", i);
		}
		tail = i;
	}

	/**
	 * 返回尾数精度
	 * @return int值
	 */
	public int getTail() {
		return tail;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#duplicate()
	 */
	@Override
	public ShowDoubleCell duplicate() {
		return new ShowDoubleCell(this);
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

		// 判断如果采用科学计数法显示时
		if (isScientific()) {
			return String.format("%E", value);
		}

		// 判断精度
		if (tail > 0) {
			String fm = "%." + String.valueOf(tail) + "f";
			return String.format(fm, value);
		} else {
			return String.format("%f", value);
		}
	}

}