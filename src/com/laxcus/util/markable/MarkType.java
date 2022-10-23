/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.markable;

import java.lang.reflect.*;

import com.laxcus.util.*;

/**
 * 标记化类型
 * 
 * @author scott.liang
 * @version 1.0 10/11/2017
 * @since laxcus 1.0
 */
public final class MarkType {

	/** 原始数据类型 **/
	public static final byte BYTE = 1;

	public static final byte BOOLEAN = 2;

	public static final byte CHAR = 3;

	public static final byte SHORT = 4;

	public static final byte INTEGER = 5;

	public static final byte LONG = 6;

	public static final byte FLOAT = 7;

	public static final byte DOUBLE = 8;

	public static final byte STRING = 9;

	/** 原始数据类型的数组格式 **/
	public static final byte BYTE_ARRAY = 11;

	public static final byte BOOLEAN_ARRAY = 12;

	public static final byte CHAR_ARRAY = 13;

	public static final byte SHORT_ARRAY = 14;

	public static final byte INTEGER_ARRAY = 15;

	public static final byte LONG_ARRAY = 16;

	public static final byte FLOAT_ARRAY = 17;

	public static final byte DOUBLE_ARRAY = 18;

	public static final byte STRING_ARRAY = 19;

	/** 可标记化 **/
	public static final byte MARKABLE = 51;

	/** 树集 **/
	public static final byte TREESET = 61;
	/** 二叉树 **/
	public static final byte TREEMAP = 62;
	/** 数组 **/
	public static final byte ARRAYLIST = 63;

	/** 串行化 **/
	public static final byte SERIALABLE = 100;

	/**
	 * 判断是标准类型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isType(byte who) {
		switch (who) {
		// 单位个值
		case MarkType.BYTE:
		case MarkType.BOOLEAN:
		case MarkType.CHAR:
		case MarkType.SHORT:
		case MarkType.INTEGER:
		case MarkType.LONG:
		case MarkType.FLOAT:
		case MarkType.DOUBLE:
			// 数组值
		case MarkType.BYTE_ARRAY:
		case MarkType.BOOLEAN_ARRAY:
		case MarkType.CHAR_ARRAY:
		case MarkType.SHORT_ARRAY:
		case MarkType.INTEGER_ARRAY:
		case MarkType.LONG_ARRAY:
		case MarkType.FLOAT_ARRAY:
		case MarkType.DOUBLE_ARRAY:
			// 字符串
		case MarkType.STRING:
		case MarkType.STRING_ARRAY:
			// 标记化
		case MarkType.MARKABLE:
			// 串行化
		case MarkType.SERIALABLE:
			// 树集
		case MarkType.TREESET:
			// 二叉树映像
		case MarkType.TREEMAP:
			// 数组
		case MarkType.ARRAYLIST:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 类类型转义为标记化类型
	 * @param type 类类型
	 * @return 标记化类型
	 */
	public static byte translate(Class<?> type) {
		if (type == java.lang.Byte.TYPE || type == java.lang.Byte.class) {
			return MarkType.BYTE;
		} else if (type == java.lang.Boolean.TYPE || type == java.lang.Boolean.class) {
			return MarkType.BOOLEAN;
		} else if (type == java.lang.Character.TYPE || type == java.lang.Character.class) {
			return MarkType.CHAR;
		} else if (type == java.lang.Short.TYPE || type == java.lang.Short.class) {
			return MarkType.SHORT;
		} else if (type == java.lang.Integer.TYPE || type == java.lang.Integer.class) {
			return MarkType.INTEGER;
		} else if (type == java.lang.Long.TYPE || type == java.lang.Long.class) {
			return MarkType.LONG;
		} else if (type == java.lang.Float.TYPE || type == java.lang.Float.class) {
			return MarkType.FLOAT;
		} else if (type == java.lang.Double.TYPE || type == java.lang.Double.class) {
			return MarkType.DOUBLE;
		}
		// 数组
		else if (type == byte[].class) {
			return MarkType.BYTE_ARRAY;
		} else if (type == boolean[].class) {
			return MarkType.BOOLEAN_ARRAY;
		} else if (type == char[].class) {
			return MarkType.CHAR_ARRAY;
		} else if (type == short[].class) {
			return MarkType.SHORT_ARRAY;
		} else if (type == int[].class) {
			return MarkType.INTEGER_ARRAY;
		} else if (type == long[].class) {
			return MarkType.LONG_ARRAY;
		} else if (type == float[].class) {
			return MarkType.FLOAT_ARRAY;
		} else if (type == double[].class) {
			return MarkType.DOUBLE_ARRAY;
		}
		// 字符串和字符串数组
		else if (type == String.class) {
			return MarkType.STRING;
		} else if (type == String[].class) {
			return MarkType.STRING_ARRAY;
		}
		// 判断是继承标记化对象
		else if (Laxkit.isInterfaceFrom(type, Markable.class)) {
			return MarkType.MARKABLE;
		}
		// 判断是树集
		else if (Laxkit.isClassFrom(type, java.util.TreeSet.class)) {
			return MarkType.TREESET;
		}
		// 如果接口是Set，属于TreeSet
		else if(Laxkit.isInterfaceFrom(type, java.util.Set.class)) {
			return MarkType.TREESET;
		}
		// 判断是二叉树映像
		else if (Laxkit.isClassFrom(type, java.util.TreeMap.class)) {
			return MarkType.TREEMAP;
		}
		// 如果接口是Map，属性TreeMap
		else if (Laxkit.isInterfaceFrom(type, java.util.Map.class)) {
			return MarkType.TREEMAP;
		}
		// 判断是数组列表
		else if (Laxkit.isClassFrom(type, java.util.ArrayList.class)) {
			return MarkType.ARRAYLIST;
		}
		else if (Laxkit.isInterfaceFrom(type, java.util.List.class)) {
			return MarkType.ARRAYLIST;
		}
		// 判断是串行化
		else if (Laxkit.isInterfaceFrom(type, java.io.Serializable.class)) {
			return MarkType.SERIALABLE;
		}

		throw new IllegalValueException("illegal class type: %s", type.getName());
	}

	/**
	 * 字段域类型转义为标记化类型
	 * @param field 字段域
	 * @return 标记化类型
	 */
	public static byte translate(Field field) {
		Class<?> type = field.getType();

		//		System.out.printf("Field is:%s - %s\n", field.getType().getName(), field.getGenericType());
		//		try {
		//		Type gt = field.getGenericType();
		//		Class<?> clazz = gt.getClass();
		//		if(clazz != java.lang.Class.class) {
		//		Object object =	clazz.newInstance();
		//		}
		//		} catch (Throwable e) {
		//			e.printStackTrace();
		//		}

		return MarkType.translate(type);
	}

	/**
	 * 对象类型黑底为标记化类型
	 * @param object 对象实例
	 * @return 标记化类型
	 */
	public static byte translate(Object object) {
		Class<?> clazz = object.getClass();
		return MarkType.translate(clazz);
	}

	/**
	 * 判断是字节类型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isByte(byte who) {
		return who == MarkType.BYTE;
	}

	/**
	 * 判断是布尔值
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isBoolean(byte who) {
		return who == MarkType.BOOLEAN;
	}

	/**
	 * 判断是字符串
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isChar(byte who) {
		return who == MarkType.CHAR;
	}

	/**
	 * 判断是短整型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isShort(byte who) {
		return who == MarkType.SHORT;
	}

	/**
	 * 判断是整型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isInteger(byte who) {
		return who == MarkType.INTEGER;
	}

	/**
	 * 判断是长整型
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isLong(byte who) {
		return who == MarkType.LONG;
	}

	/**
	 * 判断是单浮点
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isFloat(byte who) {
		return who == MarkType.FLOAT;
	}

	/**
	 * 判断是双浮点
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isDouble(byte who) {
		return who == MarkType.DOUBLE;
	}

	/**
	 * 判断是字符串
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isString(byte who) {
		return who == MarkType.STRING;
	}

	/**
	 * 判断是字节数组
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isByteArray(byte who) {
		return who == MarkType.BYTE_ARRAY;
	}

	/**
	 * 判断是布尔数组
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isBooleanArray(byte who) {
		return who == MarkType.BOOLEAN_ARRAY;
	}

	/**
	 * 判断是字符数组
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isCharArray(byte who) {
		return who == MarkType.CHAR_ARRAY;
	}

	/**
	 * 判断是短整型数组
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isShortArray(byte who) {
		return who == MarkType.SHORT_ARRAY;
	}

	/**
	 * 判断是整型数组
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isIntegerArray(byte who) {
		return who == MarkType.INTEGER_ARRAY;
	}

	/**
	 * 判断是长整型数组
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isLongArray(byte who) {
		return who == MarkType.LONG_ARRAY;
	}

	/**
	 * 判断是单浮点数组
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isFloatArray(byte who) {
		return who == MarkType.FLOAT_ARRAY;
	}

	/**
	 * 判断是双浮点数组
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isDoubleArray(byte who) {
		return who == MarkType.DOUBLE_ARRAY;
	}

	/**
	 * 判断是字符串数组
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isStringArray(byte who) {
		return who == MarkType.STRING_ARRAY;
	}

	/**
	 * 判断是标记对象
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isMarkable(byte who) {
		return who == MarkType.MARKABLE;
	}

	/**
	 * 判断是树集
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isTreeSet(byte who) {
		return who == MarkType.TREESET;
	}

	/**
	 * 判断是二叉树映像
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isTreeMap(byte who) {
		return who == MarkType.TREEMAP;
	}

	/**
	 * 判断是数组列表
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isArrayList(byte who) {
		return who == MarkType.ARRAYLIST;
	}

	/**
	 * 判断是串行化对象
	 * @param who 标记类型
	 * @return 返回真或者假
	 */
	public static boolean isSerialable(byte who) {
		return who == MarkType.SERIALABLE;
	}
}