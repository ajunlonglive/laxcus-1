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
 * 文档列对象定位器。<br>
 * 系统定义。用户也可以自己从VariableSlider下派生。
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public class DocumentSlider extends VariableSlider {

	/**
	 * 构造默认的文档列对象定位器
	 */
	public DocumentSlider() {
		super(ColumnType.DOCUMENT);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.slide.Slider#isSupport(java.lang.Object)
	 */
	@Override
	public boolean isSupport(Object e) {
		return (e.getClass() == com.laxcus.access.column.Document.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.slide.Slider#seek(java.lang.Object)
	 */
	@Override
	public java.lang.Number seek(Object e) throws SliderException {
		// 不支持弹出异常
		if (!this.isSupport(e)) {
			throw new SliderException("cannot be support %s", e.getClass().getName());
		}

		return super.doDefaultSeek((com.laxcus.access.column.Document) e);
	}

}