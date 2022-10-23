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
 * 时间戳列对象定位器。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public class TimestampSlider extends ColumnSlider {

	/**
	 * 构造默认的时间戳列对象定位器
	 */
	public TimestampSlider() {
		super(ColumnType.TIMESTAMP);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.scale.CodeScaler#isSupport(java.lang.Object)
	 */
	@Override
	public boolean isSupport(Object e) {
		return (e.getClass() == com.laxcus.access.column.Timestamp.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.scale.CodeScaler#seek(java.lang.Object)
	 */
	@Override
	public java.lang.Number seek(Object e) throws SliderException {
		// 不支持弹出异常
		if (!this.isSupport(e)) {
			throw new SliderException("cannot be support %s", e.getClass().getName());
		}

		com.laxcus.access.column.Timestamp column = (com.laxcus.access.column.Timestamp) e;
		if (column.isNull()) {
			return new java.lang.Long(0L);
		}
		return new java.lang.Long(column.getValue());
	}

}