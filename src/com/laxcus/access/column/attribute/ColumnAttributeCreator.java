/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 *  
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

import com.laxcus.access.type.*;

/**
 * 列属性生成器
 * 
 * @author scott.liang
 * @version 1.0 7/16/2012
 * @since laxcus 1.0
 */
public class ColumnAttributeCreator {

	/**
	 * 根据列属性关键字的字符串描述，返回对应的列属性实例
	 * @param family 列属性关键字的字符串描述
	 * @return ColumnAttribute实例
	 */
	public static ColumnAttribute create(String family) {
		ColumnAttribute attribute = null;
		if (family.matches("^\\s*(?i)(?:RAW|BINARY)\\s*$")) {
			attribute = new RawAttribute();
		} else if (family.matches("^\\s*(?i)(?:DOCUMENT)\\s*$")) {
			attribute = new DocumentAttribute();
		} else if (family.matches("^\\s*(?i)(?:IMAGE)\\s*$")) {
			attribute = new ImageAttribute();
		} else if (family.matches("^\\s*(?i)(?:AUDIO)\\s*$")) {
			attribute = new AudioAttribute();
		} else if (family.matches("^\\s*(?i)(?:VIDEO)\\s*$")) {
			attribute = new VideoAttribute();
		} else if (family.matches("^\\s*(?i)(?:CHAR)\\s*$")) {
			attribute = new CharAttribute();
		} else if (family.matches("^\\s*(?i)(?:WCHAR)\\s*$")) {
			attribute = new WCharAttribute();
		} else if (family.matches("^\\s*(?i)(?:HCHAR)\\s*$")) {
			attribute = new HCharAttribute();
		} else if (family.matches("^\\s*(?i)(?:SHORT|SMALLINT)\\s*$")) {
			attribute = new ShortAttribute();
		} else if (family.matches("^\\s*(?i)(?:INT|INTEGER)\\s*$")) {
			attribute = new IntegerAttribute();
		} else if (family.matches("^\\s*(?i)(?:LONG|BIGINT)\\s*$")) {
			attribute = new LongAttribute();
		} else if (family.matches("^\\s*(?i)(?:REAL|FLOAT)\\s*$")) {
			attribute = new FloatAttribute();
		} else if (family.matches("^\\s*(?i)(?:DOUBLE)\\s*$")) {
			attribute = new DoubleAttribute();
		} else if (family.matches("^\\s*(?i)(?:DATE)\\s*$")) {
			attribute = new DateAttribute();
		} else if (family.matches("^\\s*(?i)(?:TIME)\\s*$")) {
			attribute = new TimeAttribute();
		} else if (family.matches("^\\s*(?i)(?:TIMESTAMP|DATETIME)\\s*$")) {
			attribute = new TimestampAttribute();
		}
		return attribute;
	}

	/**
	 * 根据列属性类型，生成属性对象
	 * @param family 列属性类型
	 * @return ColumnAttribute实例
	 */
	public static ColumnAttribute create(byte family) {
		ColumnAttribute attribute = null;
		switch (family) {
		case ColumnType.RAW:
			attribute = new RawAttribute();
			break;
		case ColumnType.DOCUMENT:
			attribute = new DocumentAttribute();
			break;
		case ColumnType.IMAGE:
			attribute = new ImageAttribute();
			break;
		case ColumnType.AUDIO:
			attribute = new AudioAttribute();
			break;
		case ColumnType.VIDEO:
			attribute = new VideoAttribute();
			break;
		case ColumnType.CHAR:
			attribute = new CharAttribute();
			break;
		case ColumnType.WCHAR:
			attribute = new WCharAttribute();
			break;
		case ColumnType.HCHAR:
			attribute = new HCharAttribute();
			break;
		case ColumnType.SHORT:
			attribute = new ShortAttribute();
			break;
		case ColumnType.INTEGER:
			attribute = new IntegerAttribute();
			break;
		case ColumnType.LONG:
			attribute = new LongAttribute();
			break;
		case ColumnType.FLOAT:
			attribute = new FloatAttribute();
			break;
		case ColumnType.DOUBLE:
			attribute = new DoubleAttribute();
			break;
		case ColumnType.DATE:
			attribute = new DateAttribute();
			break;
		case ColumnType.TIME:
			attribute = new TimeAttribute();
			break;
		case ColumnType.TIMESTAMP:
			attribute = new TimestampAttribute();
			break;
		}
		return attribute;
	}

}