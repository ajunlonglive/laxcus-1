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
 * 双浮点列对象定位器。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public class DoubleSlider extends ColumnSlider {

	/**
	 * 构造默认的双浮点列对象定位器
	 */
	public DoubleSlider() {
		super(ColumnType.DOUBLE);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.scale.CodeScaler#isSupport(java.lang.Object)
	 */
	@Override
	public boolean isSupport(Object e) {
		return (e.getClass() == com.laxcus.access.column.Double.class);
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

		com.laxcus.access.column.Double column = (com.laxcus.access.column.Double) e;
		if (column.isNull()) {
			return new java.lang.Double(0);
		}
		return new java.lang.Double(column.getValue());
	}

}
