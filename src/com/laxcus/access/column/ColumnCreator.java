/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column;

import com.laxcus.access.type.*;
import com.laxcus.util.classable.*;

/**
 * 列实例生成器 <br>
 * 
 * @author scott.liang
 * @version 1.0 10/16/2009
 * @since laxcus 1.0
 */
public final class ColumnCreator {

	/**
	 * 根据数据类型，生成一个对应的列实例。如果数据类型全部不匹配，返回空指针(null)。
	 * @param family 列类型
	 * @return Column实例
	 */
	public static Column create(byte family) {
		switch (family) {
		case ColumnType.RAW:
			return new com.laxcus.access.column.Raw();
		case ColumnType.DOCUMENT:
			return new com.laxcus.access.column.Document();
		case ColumnType.IMAGE:
			return new com.laxcus.access.column.Image();
		case ColumnType.AUDIO:
			return new com.laxcus.access.column.Audio();
		case ColumnType.VIDEO:
			return new com.laxcus.access.column.Video();
		case ColumnType.CHAR:
			return new com.laxcus.access.column.Char();
		case ColumnType.RCHAR:
			return new com.laxcus.access.column.RChar();
		case ColumnType.WCHAR:
			return new com.laxcus.access.column.WChar();
		case ColumnType.RWCHAR:
			return new com.laxcus.access.column.RWChar();
		case ColumnType.HCHAR:
			return new com.laxcus.access.column.HChar();
		case ColumnType.RHCHAR:
			return new com.laxcus.access.column.RHChar();
		case ColumnType.SHORT:
			return new com.laxcus.access.column.Short();
		case ColumnType.INTEGER:
			return new com.laxcus.access.column.Integer();
		case ColumnType.LONG:
			return new com.laxcus.access.column.Long();
		case ColumnType.FLOAT:
			return new com.laxcus.access.column.Float();
		case ColumnType.DOUBLE:
			return new com.laxcus.access.column.Double();
		case ColumnType.DATE:
			return new com.laxcus.access.column.Date();
		case ColumnType.TIME:
			return new com.laxcus.access.column.Time();
		case ColumnType.TIMESTAMP:
			return new com.laxcus.access.column.Timestamp();
		}
		
		return null;
	}

	/**
	 * 从可类化读取器中解析一列
	 * @param reader 可类化读取器
	 * @return 返回Column实例
	 */
	public static Column resolve(ClassReader reader) {
		// 从当前字节
		byte state = reader.current();
		// 分析当前的列数据类型
		byte type = ColumnType.resolveType(state);
		Column column = ColumnCreator.create(type);
		if (column == null) {
			throw new ColumnException("illegal column type %d", type);
		}
		// 解析参数
		column.resolve(reader);
		return column;
	}
}