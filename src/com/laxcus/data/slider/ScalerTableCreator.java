/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.slider;

import com.laxcus.access.type.*;

/**
 * 列码位统计表生成器。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/23/2009
 * @since laxcus 1.0
 */
public class ScalerTableCreator {

	/**
	 * 根据传入的数据类型，建立一个默认的列码位统计表
	 * @param family 列数据类型
	 * @return 返回列码位统计表实例，没有匹配是空指针 
	 */
	public static ScalerTable create(byte family) {
		switch (family) {
		case ColumnType.SHORT:
			return new ShortScalerTable();
		case ColumnType.FLOAT:
			return new FloatScalerTable();
		case ColumnType.DOUBLE:
			return new DoubleScalerTable();
		case ColumnType.INTEGER:
		case ColumnType.DATE:
		case ColumnType.TIME:
			return new IntegerScalerTable();
		case ColumnType.RAW:
		case ColumnType.DOCUMENT:
		case ColumnType.IMAGE:
		case ColumnType.AUDIO:
		case ColumnType.VIDEO:
		case ColumnType.CHAR:
		case ColumnType.WCHAR:
		case ColumnType.HCHAR:
		case ColumnType.LONG:
		case ColumnType.TIMESTAMP:
			return new LongScalerTable();
		}
		return null;
	}
}