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
 * 单字符列对象定位器。<br>
 * 
 * 单字符列默认是UTF8编码。
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public class CharSlider extends WordSlider {

	/**
	 * 构造单字符列对象定位器
	 */
	public CharSlider() {
		super(ColumnType.CHAR);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.access.index.slide.Slider#isSupport(java.lang.Object)
	 */
	@Override
	public boolean isSupport(Object e) {
		return (e.getClass() == com.laxcus.access.column.Char.class);
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

		return doDefaultSeek(new UTF8(), (com.laxcus.access.column.Char) e);
	}


//	public static void main(String[] args) {
//		String text = "和ABC啊伙喔";
//
//		CharSlider e = new CharSlider();
//		e.setSentient(false);
//		java.lang.Number num = null;
//		try {
//			num = e.seek(text);
//			System.out.println(num);
//
//			byte[] value = new UTF8().encode(text);
//			com.laxcus.access.column.Char column = new com.laxcus.access.column.Char((short)1, value); 
//			num = e.seek(column);
//			System.out.println(num);
//		} catch(SliderException ex) {
//			ex.printStackTrace();
//		}
//	}

}