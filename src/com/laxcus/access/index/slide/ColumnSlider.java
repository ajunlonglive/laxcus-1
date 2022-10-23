/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.slide;

import com.laxcus.access.type.*;
import com.laxcus.util.*;

/**
 * 列对象定位器（或者称：列对象定位器）。<br><br>
 * 
 * 列对象定位器继承对象定位器，是对“列”数据对象的数据分割。<br><br>
 * 
 * 
 * ColumnSlider用在DataSliderPool和ColumnSector。<br>
 * <1> DataSliderPool调用它，为“列”数据对象生成码位值，并且保存在内存和磁盘上。<br>
 * <2> ColumnSector.indexOf(Column)方法中调用这个类，确定每一列所在数据分区的下标位置。<br><br>
 * 
 * 列对象定位器通过ColumnAttribute.scale属性建立关联，被DataSliderPool、ColumnSector使用。
 * 
 * @author scott.liang
 * @version 1.1 10/10/2015
 * @since laxcus 1.0
 */
public abstract class ColumnSlider implements Slider {

	/** 列数据类型 **/
	private byte family;

	/**
	 * 构造列对象定位器，指定数据类型
	 * @param family 数据类型。见Types中的定义，忽略LIKE类型。
	 */
	protected ColumnSlider(byte family) {
		super();
		setFamily(family);
	}

	/**
	 * 设置列数据类型。忽略LIKE类型。
	 * @param who 列数据类型
	 */
	private void setFamily(byte who) {
		// 判断是二进制数组
		boolean success = ColumnType.isRaw(who);
		// 判断是媒体
		if (!success) {
			success = ColumnType.isMedia(who);
		}
		// 判断是字符
		if (!success) {
			success = ColumnType.isWord(who);
		}
		// 判断是数值
		if (!success) {
			success = ColumnType.isNumber(who);
		}
		// 判断是日历
		if (!success) {
			success = ColumnType.isCalendar(who);
		}
		// 以上都不是，是错误
		if (!success) {
			throw new IllegalValueException("illegal family:%d", who);
		}
		// 保存类型
		family = who;
	}

	/**
	 * 返回对应的列数据类型
	 * @return 列数据类型
	 */
	public byte getFamily() {
		return family;
	}

}