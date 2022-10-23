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
 * 单浮点单元
 * 
 * @author scott.liang
 * @version 1.0 05/25/2013
 * @since laxcus 1.0
 */
public class ShowFloatCell extends ShowItemCell {
	
	private static final long serialVersionUID = 7346826688293783665L;

	/** 数值 **/
	private float value;

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
	 * 生成单浮点单元的数据副本
	 * @param that TableFloatCell实例
	 */
	private ShowFloatCell(ShowFloatCell that) {
		super(that);
		value = that.value;
		tail = that.tail;
		scientific = that.scientific;
	}

	/**
	 * 构造默认的单浮点单元
	 */
	private ShowFloatCell() {
		super();
		value = 0.0f;
		tail = 0;
		scientific = false;
	}

	/**
	 * 构造单浮点单元，指定索引下标
	 * @param index 索引下标
	 */
	public ShowFloatCell(int index) {
		this();
		setIndex(index);
	}
	
	/**
	 * 构造单浮点单元，指定下标和数值
	 * @param index 下标
	 * @param value 数值
	 */
	public ShowFloatCell(int index, float value) {
		this(index);
		setValue(value);
	}

	/**
	 * 构造单浮点单元，指定下标、数值、尾数精度
	 * @param index 下标
	 * @param value 数值
	 * @param tail 尾数精度
	 */
	public ShowFloatCell(int index, float value, int tail) {
		this(index, value);
		setTail(tail);
	}

	/**
	 * 设置数值
	 * @param e 数值
	 */
	public void setValue(float e) {
		value = e;
		setNullable(false);
	}
	
	/**
	 * 返回数值
	 * @return 数值
	 */
	public float getValue() {
		return value;
	}
	
	/**
	 * 设置尾数精度
	 * @param i 尾数精度
	 */
	public void setTail(int i) {
		if (i < 0) {
			throw new IllegalValueException("illegal tail %d", i);
		}
		tail = i;
	}

	/**
	 * 返回尾数精度
	 * @return 尾数精度
	 */
	public int getTail() {
		return tail;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.util.display.show.ShowItemCell#duplicate()
	 */
	@Override
	public ShowFloatCell duplicate() {
		return new ShowFloatCell(this);
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