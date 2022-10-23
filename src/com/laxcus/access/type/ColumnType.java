/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.type;

/**
 * 列数据类型，兼容JNI C接口。
 * 
 * @author scott.liang
 * @version 1.0 01/12/2016
 * @since laxcus 1.0
 */
public final class ColumnType {
	
	/** 二进制数据数组类型 **/
	public final static byte RAW = 1;

	/** 多媒体数据类型（在可变长数组上的扩展） **/
	public final static byte DOCUMENT = 2;

	public final static byte IMAGE = 3;

	public final static byte AUDIO = 4;

	public final static byte VIDEO = 5;

	/** 字符串数据类型 **/
	public final static byte CHAR = 6;

	public final static byte WCHAR = 7;

	public final static byte HCHAR = 8;

	public final static byte RCHAR = 9;

	public final static byte RWCHAR = 10;

	public final static byte RHCHAR = 11;

	/** 数值类型（固定长度） **/
	public final static byte SHORT = 12;

	public final static byte INTEGER = 13;

	public final static byte LONG = 14;

	public final static byte FLOAT = 15;

	public final static byte DOUBLE = 16;

	/** 日历类型（固定长度） **/
	public final static byte DATE = 17;

	public final static byte TIME = 18;

	public final static byte TIMESTAMP = 19;

	/**
	 * 判断是有效的列数据类型
	 * @param who 列数据类型
	 */
	public static boolean isType(byte who) {
		switch (who) {
		// 字节数组
		case ColumnType.RAW:
		// 媒体类型
		case ColumnType.DOCUMENT:
		case ColumnType.IMAGE:
		case ColumnType.AUDIO:
		case ColumnType.VIDEO:
		// 字符串
		case ColumnType.CHAR:
		case ColumnType.WCHAR:
		case ColumnType.HCHAR:
		// LIKE字符串
		case ColumnType.RCHAR:
		case ColumnType.RWCHAR:
		case ColumnType.RHCHAR:
		// 数字
		case ColumnType.SHORT:
		case ColumnType.INTEGER:
		case ColumnType.LONG:
		case ColumnType.FLOAT:
		case ColumnType.DOUBLE:
		// 时间/日期
		case ColumnType.DATE:
		case ColumnType.TIME:
		case ColumnType.TIMESTAMP:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 判断是可变长数组类型，包括二进制数组、字符串、媒体类型（LIKE字符串类型不在其中）
	 * @param who  列数据类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isVariable(byte who) {
		return (isRaw(who) || isMedia(who) || isWord(who));
	}

	/**
	 * 判断是字符类型，包括CHAR、WCHAR、HCHAR
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isWord(byte who) {
		return (isChar(who) || isWChar(who) || isHChar(who));
	}

	/**
	 * 判断是LIKE关键字，包括LIKE-CHAR、LIKE-WCHAR、LIKE-HCHAR
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isRWord(byte who) {
		return (isRChar(who) || isRWChar(who) || isRHChar(who));
	}

	/**
	 * 判断是媒体类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isMedia(byte who) {
		return (isDocument(who) || isImage(who) || isAudio(who) || isVideo(who));
	}

	/**
	 * 判断是二进制数组类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isRaw(byte who) {
		return who == ColumnType.RAW;
	}

	/**
	 * 判断是文档
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isDocument(byte who) {
		return who == ColumnType.DOCUMENT;
	}

	/**
	 * 判断是图像
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isImage(byte who) {
		return who == ColumnType.IMAGE;
	}

	/**
	 * 判断是音频
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isAudio(byte who) {
		return who == ColumnType.AUDIO;
	}

	/**
	 * 判断是视频
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isVideo(byte who) {
		return who == ColumnType.VIDEO;
	}

	/**
	 * 判断是可变长字符类型(UTF8编码)
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isChar(byte who) {
		return who == ColumnType.CHAR;
	}

	/**
	 * 判断是单字符(UTF8)LIKE类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isRChar(byte who) {
		return who == ColumnType.RCHAR;
	}

	/**
	 * 判断是可变长宽字符类型(UTF16 BigEndian编码)
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isWChar(byte who) {
		return who == ColumnType.WCHAR;
	}

	/**
	 * 判断是宽字符(UTF16 BIG-ENDIAN)的LIKE类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isRWChar(byte who) {
		return who == ColumnType.RWCHAR;
	}

	/**
	 * 判断是可变长大字符类型(UTF32编码)
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isHChar(byte who) {
		return who == ColumnType.HCHAR;
	}

	/**
	 * 判断是大字符(UTF32)LIKE关键字
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isRHChar(byte who) {
		return who == ColumnType.RHCHAR;
	}

	/**
	 * 判断是数值类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isNumber(byte who) {
		return (isShort(who) || isInteger(who) || isLong(who)
				|| isFloat(who) || isDouble(who));
	}

	/**
	 * 判断是日历类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isCalendar(byte who) {
		return (isDate(who) || isTime(who) || isTimestamp(who));
	}

	/**
	 * 判断是短型值
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isShort(byte who) {
		return who == ColumnType.SHORT;
	}

	/**
	 * 判断是整型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isInteger(byte who) {
		return who == ColumnType.INTEGER;
	}

	/**
	 * 判断是长整型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isLong(byte who) {
		return who == ColumnType.LONG;
	}

	/**
	 * 判断是单浮点类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isFloat(byte who) {
		return who == ColumnType.FLOAT;
	}

	/**
	 * 判断是双浮点类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isDouble(byte who) {
		return who == ColumnType.DOUBLE;
	}

	/**
	 * 判断是日期类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isDate(byte who) {
		return who == ColumnType.DATE;
	}

	/**
	 * 判断是时间类型
	 * @return 条件成立返回真，否则假。
	 */
	public static boolean isTime(byte who) {
		return who == ColumnType.TIME;
	}

	/**
	 * 判断是时间戳类型
	 * @return  条件成立返回真，否则假。
	 */
	public static boolean isTimestamp(byte who) {
		return who == ColumnType.TIMESTAMP;
	}
	
	/**
	 * 将字符串描述转为标记符
	 * @param input 输入的字符串描述
	 * @return 列标记
	 */
	public static byte translate(String input) {
		if (input.matches("^\\s*(?i)(RAW|BINARY)\\s*$")) {
			return ColumnType.RAW;
		} else if (input.matches("^\\s*(?i)(DOCUMENT)\\s*$")) {
			return ColumnType.DOCUMENT;
		} else if (input.matches("^\\s*(?i)(IMAGE)\\s*$")) {
			return ColumnType.IMAGE;
		} else if (input.matches("^\\s*(?i)(VIDEO)\\s*$")) {
			return ColumnType.VIDEO;
		} else if (input.matches("^\\s*(?i)(AUDIO)\\s*$")) {
			return ColumnType.AUDIO;
		} else if (input.matches("^\\s*(?i)(CHAR)\\s*$")) {
			return ColumnType.CHAR;
		} else if (input.matches("^\\s*(?i)(WCHAR)\\s*$")) {
			return ColumnType.WCHAR;
		} else if (input.matches("^\\s*(?i)(HCHAR)\\s*$")) {
			return ColumnType.HCHAR;
		} else if (input.matches("^\\s*(?i)(LIKE\\s+CHAR)\\s*$")) {
			return ColumnType.RCHAR;
		} else if (input.matches("^\\s*(?i)(LIKE\\s+WCHAR)\\s*$")) {
			return ColumnType.RWCHAR;
		} else if (input.matches("^\\s*(?i)(LIKE\\s+HCHAR)\\s*$")) {
			return ColumnType.RHCHAR;
		} else if (input.matches("^\\s*(?i)(SHORT)\\s*$")) {
			return ColumnType.SHORT;
		} else if (input.matches("^\\s*(?i)(INT|INTEGER)\\s*$")) {
			return ColumnType.INTEGER;
		} else if (input.matches("^\\s*(?i)(LONG)\\s*$")) {
			return ColumnType.LONG;
		} else if (input.matches("^\\s*(?i)(FLOAT)\\s*$")) {
			return ColumnType.FLOAT;
		} else if (input.matches("^\\s*(?i)(DOUBLE)\\s*$")) {
			return ColumnType.DOUBLE;
		} else if (input.matches("^\\s*(?i)(DATE)\\s*$")) {
			return ColumnType.DATE;
		} else if (input.matches("^\\s*(?i)(TIME)\\s*$")) {
			return ColumnType.TIME;
		} else if (input.matches("^\\s*(?i)(TIMESTAMP)\\s*$")) {
			return ColumnType.TIMESTAMP;
		} 
		
		// 无效值
		return 0;
	}

	/**
	 * 翻译列属性
	 * @param who 列属性
	 * @return 返回列属性的字符串描述
	 */
	public static String translate(byte who) {
		switch (who) {
		case ColumnType.RAW:
			return "Raw";
		case ColumnType.DOCUMENT:
			return "Document";
		case ColumnType.IMAGE:
			return "Image";
		case ColumnType.AUDIO:
			return "Audio";
		case ColumnType.VIDEO:
			return "Video";
		case ColumnType.CHAR:
			return "Char";
		case ColumnType.RCHAR:
			return "LIKE CHAR";
		case ColumnType.WCHAR:
			return "WChar";
		case ColumnType.RWCHAR:
			return "LIKE WCHAR";
		case ColumnType.HCHAR:
			return "HChar";
		case ColumnType.RHCHAR:
			return "LIKE HCHAR";
		case ColumnType.SHORT:
			return "Short";
		case ColumnType.INTEGER:
			return "Integer";
		case ColumnType.LONG:
			return "Long";
		case ColumnType.FLOAT:
			return "Float";
		case ColumnType.DOUBLE:
			return "Double";
		case ColumnType.DATE:
			return "Date";
		case ColumnType.TIME:
			return "Time";
		case ColumnType.TIMESTAMP:
			return "Timestamp";
		}
		return "INVALID";
	}

	/**
	 * 将空值标记和数据类型合并，生成一个列的状态标记。
	 * 
	 * @param nullable  空值
	 * @param family  列数据类型
	 * @return 合并码
	 */
	public static byte buildState(boolean nullable, byte family) {
		byte state = (byte) (nullable ? 1 : 0);
		state <<= 6;
		state |= (byte) (family & 0x3F);
		return state;
	}

	/**
	 * 根据列状态标记，诊断是否为空值
	 * @param state 合并码
	 * @return 返回真或者假
	 */
	public static boolean isNullable(byte state) {
		byte nullable = (byte) ((state >>> 6) & 0x3);
		return nullable == 1;
	}

	/**
	 * 根据列状态标记，解析列的数据类型
	 * @param state 合并码
	 * @return 返回真或者假
	 */
	public static byte resolveType(byte state) {
		return (byte) (state & 0x3F);
	}
}