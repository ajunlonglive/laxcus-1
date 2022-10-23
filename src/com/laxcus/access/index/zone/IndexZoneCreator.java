/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.index.zone;

import com.laxcus.access.type.*;
//import com.laxcus.access.index.balance.*;
import com.laxcus.util.*;
import com.laxcus.util.classable.*;
import com.laxcus.util.datetime.*;

/**
 * 列索引区域生成器
 * 
 * @author scott.liang
 * @version 1.1 12/06/2015
 * @since laxcus 1.0
 */
public class IndexZoneCreator {

	/**
	 * 根据列类型，建立一个默认的列索引区域
	 * @param columnFamily 列类型
	 * @return 返回关联的列索引区域，类型无效返回空指针
	 */
	public static IndexZone createDefault(byte columnFamily) {
		IndexZone zone = null;

		// 二进制字节数组
		if (ColumnType.isRaw(columnFamily)) {
			zone = new LongZone(java.lang.Byte.MIN_VALUE, java.lang.Byte.MAX_VALUE, 1);
		}
		// 媒体类型
		else if (ColumnType.isMedia(columnFamily)) {
			zone = new LongZone(0, java.lang.Long.MAX_VALUE, 1);
		}
		// 字符类型（为了兼容，放大到LONG类型，实际在INTEGER范围内）
		else if (ColumnType.isWord(columnFamily)) {
			zone = new LongZone(0, java.lang.Long.MAX_VALUE, 1);
		}
		// 数值类型
		else if (ColumnType.isShort(columnFamily)) {
			zone = new ShortZone(java.lang.Short.MIN_VALUE, java.lang.Short.MAX_VALUE, 1);
		} else if (ColumnType.isInteger(columnFamily)) {
			zone = new IntegerZone(java.lang.Integer.MIN_VALUE, java.lang.Integer.MAX_VALUE, 1);
		} else if (ColumnType.isLong(columnFamily)) {
			zone = new LongZone(java.lang.Long.MIN_VALUE, java.lang.Long.MAX_VALUE, 1);
		} else if (ColumnType.isFloat(columnFamily)) {
			zone = new FloatZone(-3.4028235e+38f, 3.4028235e+38f, 1);
		} else if (ColumnType.isDouble(columnFamily)) {
			zone = new DoubleZone(-1.7976931348623157e+308, 1.7976931348623157e+308, 1);
		}
		// 日期/时间类型
		else if (ColumnType.isDate(columnFamily)) {
			zone = new IntegerZone(SimpleDate.MIN_VALUE, SimpleDate.MAX_VALUE, 1);
		} else if (ColumnType.isTime(columnFamily)) {
			zone = new IntegerZone(SimpleTime.MIN_VALUE, SimpleTime.MAX_VALUE, 1);
		} else if (ColumnType.isTimestamp(columnFamily)) {
			zone = new LongZone(SimpleTimestamp.MIN_VALUE, SimpleTimestamp.MAX_VALUE, 1);
		}

		return zone;
	}
	
	/**
	 * 解析索引范围，返回对应的实例
	 * @param reader 可类化数据读取器
	 * @return 返回IndexZone子类实例
	 * @throws IllegalValueException 出错弹出异常
	 */
	public static IndexZone resolve(ClassReader reader) {
		// 索引范围类型（可类化读取器不移动数据下标）
		byte family = reader.current();
		// 判断
		switch (family) {
		case IndexZoneTag.SHORT_ZONE:
			return new ShortZone(reader);
		case IndexZoneTag.INTEGER_ZONE:
			return new IntegerZone(reader);
		case IndexZoneTag.LONG_ZONE:
			return new LongZone(reader);
		case IndexZoneTag.FLOAT_ZONE:
			return new FloatZone(reader);
		case IndexZoneTag.DOUBLE_ZONE:
			return new DoubleZone(reader);
		}

		throw new IllegalValueException("illegal family: %d", family);
	}

}