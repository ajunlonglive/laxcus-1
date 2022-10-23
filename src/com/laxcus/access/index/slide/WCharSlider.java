/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.slide;

import com.laxcus.access.type.*;
import com.laxcus.util.charset.*;

/**
 * 宽字符对象定位器。<br>
 * 
 * 宽字符列是UTF16编码。这是系统默认使用的宽字符对象定位器。
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public class WCharSlider extends WordSlider {

	/**
	 * 构造默认的宽字符对象定位器
	 */
	public WCharSlider() {
		super(ColumnType.WCHAR);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.scale.CodeScaler#isSupport(java.lang.Object)
	 */
	@Override
	public boolean isSupport(Object e) {
		return (e.getClass() == com.laxcus.access.column.WChar.class);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.scale.CodeScaler#seek(java.lang.Object)
	 */
	@Override
	public java.lang.Number seek(Object e) throws SliderException {
		// 不支持弹出异常
		if (!isSupport(e)) {
			throw new SliderException("cannot be support %s", e.getClass().getName());
		}

		return doDefaultSeek(new UTF16(), (com.laxcus.access.column.WChar) e);
	}

}