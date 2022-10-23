/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.util;

import java.math.*;
import java.util.regex.*;

import com.laxcus.access.column.*;
import com.laxcus.access.column.attribute.*;

/**
 * 数值格式生成器。
 * 
 * @author scott.liang
 * @version 1.0 6/12/2009
 * @since laxcus 1.0
 */
public final class NumberGenerator {

	private final static String REGEX_SHORT = "^\\s*([+|-]{0,1}[0-9]{1,5})\\s*$";
	private final static String REGEX_INTEGER = "^\\s*([+|-]{0,1}[0-9]{1,10})\\s*$";
	private final static String REGEX_LONG = "^\\s*([+|-]{0,1}[0-9]{1,19})\\s*$";
	private final static String REGEX_DECIMAL =  "^\\s*([+|-]{0,1}[0-9]+[\\.]{0,1}[0-9]*)\\s*$";
	private final static String REGEX_DECIMAL2 = "^\\s*([+|-]{0,1}[0-9]+[\\.]{0,1}[0-9]+E\\+[0-9]+)\\s*$";

	/**
	 * 解析并且返回短整型值
	 * @param input 输入字符串
	 * @return 返回解析的短整型值
	 */
	public static short splitShort(String input) {
		Pattern pattern = Pattern.compile(NumberGenerator.REGEX_SHORT);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new ColumnException();
		}

		String s = matcher.group(1);
		if (s.charAt(0) == '+') s = s.substring(1);

		BigInteger value = new BigInteger(s);
		BigInteger min = new BigInteger(java.lang.Short.toString(java.lang.Short.MIN_VALUE));
		BigInteger max = new BigInteger(java.lang.Short.toString(java.lang.Short.MAX_VALUE));
		if (min.compareTo(value) <= 0 && value.compareTo(max) <= 0) {
			return value.shortValue();
		}

		throw new ColumnException();
	}

	/**
	 * 解析并且返回整型值
	 * @param input 输入字符串
	 * @return 返回解析的整型值
	 */
	public static int splitInt(String input) {
		Pattern pattern = Pattern.compile(NumberGenerator.REGEX_INTEGER);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new ColumnException("illegal int:%s", input);
		}

		String s = matcher.group(1);
		if (s.charAt(0) == '+') s = s.substring(1);

		BigInteger value = new BigInteger(s);
		BigInteger min = new BigInteger(java.lang.Integer.toString(java.lang.Integer.MIN_VALUE));
		BigInteger max = new BigInteger(java.lang.Integer.toString(java.lang.Integer.MAX_VALUE));
		if (min.compareTo(value) <= 0 && value.compareTo(max) <= 0) {
			return value.intValue();
		}

		throw new ColumnException("int %s out!", input);
	}

	/**
	 * 解析并且返回长整型值
	 * @param input 输入字符串
	 * @return 返回解析的长整型值
	 */
	public static long splitLong(String input) {
		Pattern pattern = Pattern.compile(NumberGenerator.REGEX_LONG);
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) {
			throw new ColumnException("illegal long:%s", input);
		}

		String s = matcher.group(1);
		if (s.charAt(0) == '+') s = s.substring(1);

		BigInteger value = new BigInteger(s);
		BigInteger min = new BigInteger(java.lang.Long.toString(java.lang.Long.MIN_VALUE));
		BigInteger max = new BigInteger(java.lang.Long.toString(java.lang.Long.MAX_VALUE));
		if (min.compareTo(value) <= 0 && value.compareTo(max) <= 0) {
			return value.longValue();
		}

		throw new ColumnException("long %s out!", input);
	}

	/**
	 * 解析并且返回单浮点值
	 * @param input 输入字符串
	 * @return 返回解析的单浮点值
	 */
	public static float splitFloat(String input) {
		Pattern pattern = Pattern.compile(NumberGenerator.REGEX_DECIMAL);
		Matcher matcher = pattern.matcher(input);

		// 如果不匹配...
		if (!matcher.matches()) {
			// 科学计数法
			pattern = Pattern.compile(NumberGenerator.REGEX_DECIMAL2);
			matcher = pattern.matcher(input);
			// 判断出错
			if (!matcher.matches()) {
				throw new ColumnException("illegal float:%s", input);
			}
		}

		String s = matcher.group(1);
		if (s.charAt(0) == '+') s = s.substring(1);
		
		BigDecimal value = new BigDecimal(s);
		return value.floatValue();
		
//		BigDecimal value = new BigDecimal(s);
//		BigDecimal min = new BigDecimal(java.lang.Float.toString(java.lang.Float.MIN_VALUE));
//		BigDecimal max = new BigDecimal(java.lang.Float.toString(java.lang.Float.MAX_VALUE));
//
//		if (min.compareTo(value) <= 0 && value.compareTo(max) <= 0) {
//			return value.floatValue();
//		}
//
//		throw new ColumnException("float %s out!", input);
	}

	/**
	 * 解析并且返回双浮点值
	 * @param input 输入字符串
	 * @return 返回解析的双浮点值
	 */
	public static double splitDouble(String input) {
		Pattern pattern = Pattern.compile(NumberGenerator.REGEX_DECIMAL);
		Matcher matcher = pattern.matcher(input);
		// 如果不匹配...
		if (!matcher.matches()) {
			// 科学计数法
			pattern = Pattern.compile(NumberGenerator.REGEX_DECIMAL2);
			matcher = pattern.matcher(input);
			// 判断出错
			if (!matcher.matches()) {
				throw new ColumnException("illegal double:%s", input);
			}
		}
		//		if (!matcher.matches()) {
		//			throw new ColumnException("illegal double:%s", input);
		//		}

		String s = matcher.group(1);
		if (s.charAt(0) == '+') s = s.substring(1);
		
		BigDecimal value = new BigDecimal(s);
		return value.doubleValue();
		

//		BigDecimal value = new BigDecimal(s);
//		BigDecimal min = new BigDecimal(java.lang.Double.toString(java.lang.Double.MIN_VALUE));
//		BigDecimal max = new BigDecimal(java.lang.Double.toString(java.lang.Double.MAX_VALUE));
//
//		if (min.compareTo(value) <= 0 && value.compareTo(max) <= 0) {
//			return value.doubleValue();
//		}
//
//		throw new ColumnException("double %s out!", input);
	}

	/**
	 * 生成一个短整型列
	 * @param attribute 短整型列属性
	 * @param value 字符串值
	 * @return 短整型列
	 */
	public static com.laxcus.access.column.Short createShort(
			ShortAttribute attribute, String value) {
		short num = NumberGenerator.splitShort(value);
		return new com.laxcus.access.column.Short(attribute.getColumnId(), num);
	}

	/**
	 * 生成一个短整型列
	 * @param attribute 短整型列属性
	 * @param value 短整型值
	 * @return 短整型列
	 */
	public static com.laxcus.access.column.Short createShort(
			ShortAttribute attribute, short value) {
		return new com.laxcus.access.column.Short(attribute.getColumnId(), value);
	}

	/**
	 * 生成一个整型列
	 * @param attribute 整型列属性
	 * @param value 字符串值
	 * @return 整型列
	 */
	public static com.laxcus.access.column.Integer createInteger(
			IntegerAttribute attribute, String value) {
		int num = NumberGenerator.splitInt(value);
		return new com.laxcus.access.column.Integer(attribute.getColumnId(), num);
	}

	/**
	 * 生成一个整型列
	 * @param attribute 整型列属性
	 * @param value 整型值
	 * @return 整型列
	 */
	public static com.laxcus.access.column.Integer createInteger(
			IntegerAttribute attribute, int value) {
		return new com.laxcus.access.column.Integer(attribute.getColumnId(), value);
	}

	/**
	 * 生成一个长整型列
	 * @param attribute 长整型列属性
	 * @param value 字符串值
	 * @return 长整型列
	 */
	public static com.laxcus.access.column.Long createLong(
			LongAttribute attribute, String value) {
		long num = NumberGenerator.splitLong(value);
		return new com.laxcus.access.column.Long(attribute.getColumnId(), num);
	}

	/**
	 * 生成一个长整型列
	 * @param attribute 长整型列属性
	 * @param value 长整型值
	 * @return 长整型列
	 */
	public static com.laxcus.access.column.Long createLong(
			LongAttribute attribute, long value) {
		return new com.laxcus.access.column.Long(attribute.getColumnId(), value);
	}

	/**
	 * 生成一个单浮点列
	 * @param attribute 单浮点列属性
	 * @param value 字符串值
	 * @return 单浮点列
	 */
	public static com.laxcus.access.column.Float createFloat(
			FloatAttribute attribute, String value) {
		float num = NumberGenerator.splitFloat(value);
		return new com.laxcus.access.column.Float(attribute.getColumnId(), num);
	}

	/**
	 * 生成一个单浮点列
	 * @param attribute 单浮点列属性
	 * @param value 单浮点列
	 * @return 单浮点列
	 */
	public static com.laxcus.access.column.Float createFloat(
			FloatAttribute attribute, float value) {
		return new com.laxcus.access.column.Float(attribute.getColumnId(), value);
	}

	/**
	 * 生成一个双浮点列
	 * @param attribute 双浮点列属性
	 * @param value 字符串值
	 * @return 双浮点列
	 */
	public static com.laxcus.access.column.Double createDouble(
			DoubleAttribute attribute, String value) {
		double num = NumberGenerator.splitDouble(value);
		return new com.laxcus.access.column.Double(attribute.getColumnId(), num);
	}

	/**
	 * 生成一个双浮点列
	 * @param attribute 双浮点列属性
	 * @param value 双浮点列
	 * @return 双浮点列
	 */
	public static com.laxcus.access.column.Double createDouble(
			DoubleAttribute attribute, double value) {
		return new com.laxcus.access.column.Double(attribute.getColumnId(), value);
	}

}