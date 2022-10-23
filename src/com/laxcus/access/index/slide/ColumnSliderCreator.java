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
 * 列对象定位器建立器。
 * 
 * @author scott.liang
 * @version 1.0 6/10/2013
 * @since laxcus 1.0
 */
public class ColumnSliderCreator {

	/**
	 * 根据数据类型，建立一个对应的列对象定位器
	 * @param family 数据类型
	 * @return 返回“ColumnSlider”子类实例；不匹配返回空指针。
	 */
	public static ColumnSlider create(byte family) {
		switch (family) {
		// 二进制字节数组列
		case ColumnType.RAW:
			return new RawSlider();
		// 多媒体列
		case ColumnType.DOCUMENT:
			return new DocumentSlider();
		case ColumnType.IMAGE:
			return new ImageSlider();
		case ColumnType.AUDIO:
			return new AudioSlider();
		case ColumnType.VIDEO:
			return new VideoSlider();
		// 字符列
		case ColumnType.CHAR:
			return new CharSlider();
		case ColumnType.WCHAR:
			return new WCharSlider();
		case ColumnType.HCHAR:
			return new HCharSlider();
		// 数值列
		case ColumnType.SHORT:
			return new ShortSlider();
		case ColumnType.INTEGER:
			return new IntegerSlider();
		case ColumnType.LONG:
			return new LongSlider();
		case ColumnType.FLOAT:
			return new FloatSlider();
		case ColumnType.DOUBLE:
			return new DoubleSlider();
		// 日期列
		case ColumnType.DATE:
			return new DateSlider();
		case ColumnType.TIME:
			return new TimeSlider();
		case ColumnType.TIMESTAMP:
			return new TimestampSlider();
		}
		return null;
	}
}
