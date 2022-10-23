/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.slide;

import com.laxcus.access.type.*;

/**
 * 日期列对象定位器。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public class DateSlider extends ColumnSlider {

	/**
	 * 构造默认的日期列对象定位器
	 */
	public DateSlider() {
		super(ColumnType.DATE);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.slide.Slider#isSupport(java.lang.Object)
	 */
	@Override
	public boolean isSupport(Object e) {
		return (e.getClass() == com.laxcus.access.column.Date.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.slide.Slider#seek(java.lang.Object)
	 */
	@Override
	public java.lang.Number seek(Object e) throws SliderException {
		// 不支持弹出异常
		if (!isSupport(e)) {
			throw new SliderException("cannot be support %s", e.getClass().getName());
		}

		com.laxcus.access.column.Date column = (com.laxcus.access.column.Date) e;
		// 如果是空值
		if (column.isNull()) {
			return new java.lang.Integer(0);
		}
		return new java.lang.Integer(column.getValue());
	}

}
