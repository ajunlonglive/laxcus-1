/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import java.math.*;
import java.net.*;
import java.util.regex.*;

import com.laxcus.util.color.*;
import com.laxcus.util.net.*;

/**
 * 配置参数解析器 <br>
 * 
 * 解析配置文件中以文本表述的各种参数单元
 * 
 * @author scott.liang
 * @version 1.2 12/16/2015
 * @since laxcus 1.0
 */
public final class ConfigParser {
	
	/** 毫秒/微秒时间 **/
	private final static String NANOTIME_REGEX = "^\\s*([0-9][0-9]*)\\s*(?i)(MILLISECOND|MILLISECONDS|MILLI\\s+SECOND|MILLI\\s+SECONDS|MS|毫秒|MICROSECOND|MICROSECONDS|MICRO\\s+SECOND|MICRO\\s+SECONDS|MMS|微秒)\\s*$";
	
	/**
	 * 判断微秒时间语法
	 * @param input 输入文本
	 * @return 匹配返回真，否则假
	 */
	public static boolean isMicroTime(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		Pattern pattern = Pattern.compile(ConfigParser.NANOTIME_REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，返回默认时间
		return matcher.matches();
	}
	
	/**
	 * 解析微秒时间参数，支持毫秒和微秒(毫微秒)的后缀参数
	 * @param input 输入语句
	 * @param defaultTime 默认值
	 * @return 返回以微秒计的时间
	 */
	public static int splitMicroTime(String input, int defaultTime) {
		if (input == null) {
			return defaultTime;
		}

		Pattern pattern = Pattern.compile(ConfigParser.NANOTIME_REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，返回默认时间
		if (!matcher.matches()) {
			return defaultTime;
		}

		// 解析时间
		int time = Integer.parseInt(matcher.group(1));
		// 小于等于0时，返回-1。
		if (time < 0) {
			return -1;
		}
		
		String unit = matcher.group(2);
		// 判断是毫秒，否则就是微秒
		if (unit.matches("^\\s*(?i)(MILLISECOND|MILLISECONDS|MILLI\\s+SECOND|MILLI\\s+SECONDS|MS|毫秒)\\s*$")) {
			time = time * 1000;
		}
		return time;
	}
	
	
	/** 时间正则表达式 **/
	private final static String TIME_REGEX = "^\\s*([\\-]*[0-9]+)\\s*(?i)(HOUR|HOURS|H|小时|时|MINUTE|MINUTES|M|分钟|分|SECOND|SECONDS|S|秒|MILLISECOND|MILLISECONDS|MILLI\\s+SECOND|MILLI\\s+SECONDS|MS|毫秒)\\s*$";
	
	/**
	 * 判断匹配时间语法
	 * @param input 输入文本
	 * @return 匹配返回真，否则假
	 */
	public static boolean isTime(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		Pattern pattern = Pattern.compile(ConfigParser.TIME_REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，返回默认时间
		return matcher.matches();
	}

	/**
	 * 解析时间参数配置 <br>
	 * 时间参数后缀分别以：H|HOUR|HOURS|小时|时、M|MINUTE|MINUTES|分|分钟、S|SECOND|SECONDS|秒、MS|毫秒表述。如果前缀数字小于0时，将返回-1。<br>
	 * 如：23HOUR、120S、3M、-1H都是合法的格式。如果时间参数不符合格式，将返回默认值。<br>
	 * 
	 * @param input 输入文本
	 * @param defaultTime 默认值
	 * @return 返回以毫秒为单位的时间值
	 */
	public static long splitTime(String input, long defaultTime) {
		if (input == null) {
			return defaultTime;
		}

		Pattern pattern = Pattern.compile(ConfigParser.TIME_REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，返回默认时间
		if (!matcher.matches()) {
			return defaultTime;
		}

		// 计算组件扫描间隔时间
		long timeout = Long.parseLong(matcher.group(1));
		// 小于等于0时，返回-1。
		if (timeout < 0) {
			return -1L;
		}

		String unit = matcher.group(2);
		// 判断时间类型
		if (unit.matches("^\\s*(?i)(HOUR|HOURS|H|小时|时)\\s*$")) {
			timeout = timeout * 60 * 60 * 1000;
		} else if (unit.matches("^\\s*(?i)(M|MINUTE|MINUTES|分钟|分)\\s*$")) {
			timeout = timeout * 60 * 1000;
		} else if (unit.matches("^\\s*(?i)(S|SECOND|SECONDS|秒)\\s*$")) {
			timeout = timeout * 1000;
		} else if (unit.matches("^\\s*(?i)(MILLISECOND|MILLISECONDS|MILLI\\s+SECOND|MILLI\\s+SECONDS|MS|毫秒)\\s*$")) {

		}

		return timeout;
	}
	
	/** 容量正则表达式 **/
	private final static String CAPACITY_REGEX_LONG = "^\\s*(\\d+)\\s*(?i)(E|EB|P|PB|T|TB|G|GB|M|MB|K|KB|B|BYTE|BYTES|\\s*)\\s*$";
	
	/**
	 * 判断是整数容量语法
	 * @param input 输入文本
	 * @return 匹配返回真，否则假
	 */
	public static boolean isLongCapacity(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		Pattern pattern = Pattern.compile(ConfigParser.CAPACITY_REGEX_LONG);
		Matcher matcher = pattern.matcher(input);
		// 返回真或者假
		return matcher.matches();
	}

	/**
	 * 解析容量参数 <br>
	 * 容量包括内存容量和硬盘容量两种可能，后缀分别以：G、GB、M、MB、K、KB、B、BYTE、BYTES表述。
	 * @param input 输入文本
	 * @param defaultValue 默认值
	 * 
	 * @return 返回以字节为单位的容量长整数
	 */
	public static long splitLongCapacity(String input, long defaultValue) {
		if (input == null) {
			return defaultValue;
		}

		Pattern pattern = Pattern.compile(ConfigParser.CAPACITY_REGEX_LONG);
		Matcher matcher = pattern.matcher(input);
		// 不成功，使用默认值
		if (!matcher.matches()) {
			return defaultValue;
		}
		// 解析容量尺寸
		long size = Long.parseLong(matcher.group(1));
		String unit = matcher.group(2);

		if (unit.matches("^\\s*(?i)(E|EB)\\s*$")) {
			size = size * Laxkit.EB;
		} else if (unit.matches("^\\s*(?i)(P|PB)\\s*$")) {
			size = size * Laxkit.PB;
		} else if (unit.matches("^\\s*(?i)(T|TB)\\s*$")) {
			size = size * Laxkit.TB;
		} else if (unit.matches("^\\s*(?i)(G|GB)\\s*$")) {
			size = size * Laxkit.GB;
		} else if (unit.matches("^\\s*(?i)(M|MB)\\s*$")) {
			size = size * Laxkit.MB;
		} else if (unit.matches("^\\s*(?i)(K|KB)\\s*$")) {
			size = size * Laxkit.KB;
		}
		return size;
	}
	
	/**
	 * 将数据容量翻译为字符串描述
	 * @param size 数据长度
	 * @param tail 小数点后尾数
	 * @return 数据长度的字符串描述
	 */
	public static String splitCapacity(long size, int tail) {
		if (size < 0) {
			return String.format("%d", size);
		} else if (size == 0) {
			return "0";
		}

		// 转义
		if (size >= Laxkit.EB) {
			if (size % Laxkit.EB == 0) {
				return String.format("%dEB", size / Laxkit.EB);
			}
			String fm = "%." + String.valueOf(tail) + "fEB";
			return String.format(fm, (double) size / (double) Laxkit.EB);
		} else if (size >= Laxkit.PB) {
			if (size % Laxkit.PB == 0) {
				return String.format("%dPB", size / Laxkit.PB);
			}
			String fm = "%." + String.valueOf(tail) + "fPB";
			return String.format(fm, (double) size / (double) Laxkit.PB);
		} else if (size >= Laxkit.TB) {
			if (size % Laxkit.TB == 0) {
				return String.format("%dTB", size / Laxkit.TB);
			}
			String fm = "%." + String.valueOf(tail) + "fTB";
			return String.format(fm, (double) size / (double) Laxkit.TB);
		} else if (size >= Laxkit.GB) {
			if (size % Laxkit.GB == 0) {
				return String.format("%dGB", size / Laxkit.GB);
			}
			String fm = "%." + String.valueOf(tail) + "fGB";
			return String.format(fm, (double) size / (double) Laxkit.GB);
		} else if (size >= Laxkit.MB) {
			if (size % Laxkit.MB == 0) {
				return String.format("%dMB", size / Laxkit.MB);
			}
			String fm = "%." + String.valueOf(tail) + "fMB";
			return String.format(fm, (double) size / (double) Laxkit.MB);
		} else if (size >= Laxkit.KB) {
			if (size % Laxkit.KB == 0) {
				return String.format("%dKB", size / Laxkit.KB);
			}
			String fm = "%." + String.valueOf(tail) + "fKB";
			return String.format(fm, (double) size / (double) Laxkit.KB);
		} else {
			return String.format("%d", size);
		}
	}
	
	/**
	 * 将数据容量翻译为字符串描述，默认小数点后3位。
	 * @param size 数据长度
	 * @return 数据长度的字符串描述
	 */
	public static String splitCapacity(long size) {
		return ConfigParser.splitCapacity(size, 2);
	}
	
	/** 容量正则表达式2 **/
	private final static String CAPACITY_REGEX_DOUBLE = "^\\s*([0-9]+[\\.0-9]*)\\s*(?i)(E|EB|P|PB|T|TB|G|GB|M|MB|K|KB|B|BYTE|BYTES|\\s*)\\s*$";
	
	/**
	 * 判断是浮点容量语法
	 * @param input 输入语句
	 * @return 匹配返回真，否则假
	 */
	public static boolean isDoubleCapacity(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		Pattern pattern = Pattern.compile(ConfigParser.CAPACITY_REGEX_DOUBLE);
		Matcher matcher = pattern.matcher(input);
		// 不成功，使用默认值
		return matcher.matches();
	}
	
	/**
	 * 解析容量参数 <br>
	 * 容量包括内存容量和硬盘容量两种可能，后缀分别以：E、EB、P、PB、T、TB、G、GB、M、MB、K、KB、B、BYTE、BYTES表述。
	 * @param input 输入文本
	 * @param defaultValue 默认值
	 * 
	 * @return 返回浮点数的字节为单位的容量长整数
	 */
	public static double splitDoubleCapacity(String input, double defaultValue) {
		if (input == null) {
			return defaultValue;
		}

		Pattern pattern = Pattern.compile(ConfigParser.CAPACITY_REGEX_DOUBLE);
		Matcher matcher = pattern.matcher(input);
		// 不成功，使用默认值
		if (!matcher.matches()) {
			return defaultValue;
		}
		// 解析容量尺寸
		double size = Double.parseDouble(matcher.group(1));
		String unit = matcher.group(2);

		if (unit.matches("^\\s*(?i)(E|EB)\\s*$")) {
			size = size * Laxkit.EB;
		} else if (unit.matches("^\\s*(?i)(P|PB)\\s*$")) {
			size = size * Laxkit.PB;
		} else if (unit.matches("^\\s*(?i)(T|TB)\\s*$")) {
			size = size * Laxkit.TB;
		} else if (unit.matches("^\\s*(?i)(G|GB)\\s*$")) {
			size = size * Laxkit.GB;
		} else if (unit.matches("^\\s*(?i)(M|MB)\\s*$")) {
			size = size * Laxkit.MB;
		} else if (unit.matches("^\\s*(?i)(K|KB)\\s*$")) {
			size = size * Laxkit.KB;
		}
		return size;
	}
	
	/** 比率正则表达式 **/
	private final static String RATE_REGEX = "^\\s*([0-9]+[\\.0-9]*)\\s*(?:\\%)\\s*$";
	
	/**
	 * 判断是比率参数语句
	 * @param input 输入文本
	 * @return 匹配返回真，否则假
	 */
	public static boolean isRate(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		Pattern pattern = Pattern.compile(ConfigParser.RATE_REGEX);
		Matcher matcher = pattern.matcher(input);
		// 判断匹配
		return matcher.matches();
	}
	
	/**
	 * 解析比率参数。
	 * 这将返回一个双浮点值。
	 * @param input 输入文本
	 * @param defaultValue 默认值
	 * @return 返回解析的参数
	 */
	public static double splitRate(String input, double defaultValue) {
		if (input == null) {
			return defaultValue;
		}

		Pattern pattern = Pattern.compile(ConfigParser.RATE_REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，返回默认值
		if (!matcher.matches()) {
			return defaultValue;
		}
		return Double.parseDouble(matcher.group(1));
	}
	
	/**
	 * 解析有效比率
	 * @param availables 有效数
	 * @param max 最大数
	 * @param tail 尾数
	 * @return 返回比率的字符串描述
	 */
	public static String splitRate(long availables, long max, int tail) {
		String rate = "";
		if (availables == max) {
			rate = "100%";
		} else if (availables < 1) {
			rate = "0.00%";
		} else {
			// 记录尾数
			String fm = "%." + String.valueOf(tail) + "f";
			double value = ((double) availables / (double) max) * 100.0f;
			rate = String.format(fm, value) + "%";
		}
		return rate;
	}
	
	/**
	 * 解析有效比率
	 * @param availables 有效数
	 * @param max 最大数
	 * @return 返回比率的字符串描述
	 */
	public static String splitRate(long availables, long max) {
		return ConfigParser.splitRate(availables, max, 2);
	}
	
	/**
	 * 解析逻辑值，允许“YES、TRUE、是”和“NO、NOT、FALSE、否、不是”两类关键字
	 * @param input 输入文本
	 * @return 是或者否
	 */
	public static boolean isBoolean(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		if (input.matches("^\\s*(?i)(Y|YES|TRUE|是)\\s*$")) {
			return true;
		} else if (input.matches("^\\s*(?i)(N|NO|NOT|FALSE|否|不是)\\s*$")) {
			return true;
		}
		return false;
	}

	/**
	 * 解析逻辑值，允许“YES、TRUE、是”和“NO、NOT、FALSE、否、不是”两类关键字
	 * @param input 输入文本
	 * @param defaultValue 默认值，当以上条件不成立时
	 * @return 返回解析的逻辑值
	 */
	public static boolean splitBoolean(String input, boolean defaultValue) {
		if (input == null || input.trim().isEmpty()) {
			return defaultValue;
		}

		if (input.matches("^\\s*(?i)(Y|YES|TRUE|是)\\s*$")) {
			return true;
		} else if (input.matches("^\\s*(?i)(N|NO|NOT|FALSE|否|不是)\\s*$")) {
			return false;
		} else {
			return defaultValue;
		}
	}
	
	/** 16进制整数, 最大4个字符 **/
	private static final String XDIGIT_SHORT = "^\\s*(?i)(?:0x|x|#)\\s*([0-9A-Fa-f]{1,4})\\s*$";
	/** 10进制整数, 最大5个字符 **/
	private static final String DIGIT_SHORT = "^\\s*([\\-]{0,1}[0-9]{1,5})\\s*$";

	/**
	 * 判断是整数
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public static boolean isShort(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		// 如果是16进制数字
		Pattern pattern = Pattern.compile(ConfigParser.XDIGIT_SHORT);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			// 普通10进制数字，允许有负数
			pattern = Pattern.compile(ConfigParser.DIGIT_SHORT);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}


	/** 16进制整数, 最大8个字符 **/
	private static final String XDIGIT_INT = "^\\s*(?i)(?:0x|x|#)\\s*([0-9A-Fa-f]{1,8})\\s*$";
	/** 10进制整数, 最大10个字符 **/
	private static final String DIGIT_INT = "^\\s*([\\-]{0,1}[0-9]{1,10})\\s*$";

	/**
	 * 判断是整数
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public static boolean isInteger(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		// 如果是16进制数字
		Pattern pattern = Pattern.compile(ConfigParser.XDIGIT_INT);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			// 普通10进制数字，允许有负数
			pattern = Pattern.compile(ConfigParser.DIGIT_INT);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		return success;
	}

	/**
	 * 解析整数数字。<br>
	 * 包括16进制和10进制两种格式，10进制数字允许负数（前缀带“-”）。如果不匹配，将返回默认值
	 * 
	 * @param input 输入文本
	 * @param defaultValue 默认值
	 * @return 返回解析的数值
	 */
	public static int splitInteger(String input, int defaultValue) {
		if (input == null || input.trim().isEmpty()) {
			return defaultValue;
		}

		// 如果是16进制数字
		Pattern pattern = Pattern.compile(ConfigParser.XDIGIT_INT);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return new BigInteger(matcher.group(1), 16).intValue();
		}

		// 普通10进制数字，允许有负数
		pattern = Pattern.compile(ConfigParser.DIGIT_INT);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return new BigInteger(matcher.group(1), 10).intValue();
		}
		// 不匹配，返回默认值
		return defaultValue;
	}

	/** 16进制长整数，最大16个字符 **/
	private static final String XDIGIT_LONG = "^\\s*(?i)(?:0x|x|#)\\s*([0-9A-Fa-f]{1,16})\\s*$";
	/** 10进制长整数，最大19个字符 **/
	private static final String DIGIT_LONG = "^\\s*([\\-]{0,1}[0-9]{1,19})\\s*$";

	/**
	 * 判断是长整型
	 * @param input 输入文本
	 * @return 返回真或者假
	 */
	public static boolean isLong(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}
		// 两种语句格式
		Pattern pattern = Pattern.compile(ConfigParser.XDIGIT_LONG);
		Matcher matcher = pattern.matcher(input);
		boolean success = matcher.matches();
		if (!success) {
			// 普通10进制数字，允许有负数
			pattern = Pattern.compile(ConfigParser.DIGIT_LONG);
			matcher = pattern.matcher(input);
			success = matcher.matches();
		}
		// 返回结果
		return success;
	}

	/**
	 * 解析长整数数字。<br>
	 * 包括16进制和10进制两种格式，10进制数字允许负数（前缀带“-”）。如果不匹配，将返回默认值
	 * 
	 * @param input 输入文本
	 * @param defaultValue 默认值
	 * @return 返回解析的数值
	 */
	public static long splitLong(String input, long defaultValue) {
		if (input == null || input.trim().isEmpty()) {
			return defaultValue;
		}

		// 如果是16进制数字
		Pattern pattern = Pattern.compile(ConfigParser.XDIGIT_LONG);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return new BigInteger(matcher.group(1), 16).longValue();
		}

		// 普通10进制数字，允许有负数
		pattern = Pattern.compile(ConfigParser.DIGIT_LONG);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			return new BigInteger(matcher.group(1), 10).longValue();
		}
		// 不匹配，返回默认值
		return defaultValue;
	}
	
	/** 单浮点表示 **/
	private final static String REGEX_FLOAT = "^\\s*([0-9]+[\\.0-9]*)\\s*(?i)(?:F*)\\s*$";
	
	/**
	 * 判断是浮点语法
	 * @param input 输入语句
	 * @return 匹配返回真，否则假
	 */
	public static boolean isFloat(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		Pattern pattern = Pattern.compile(ConfigParser.REGEX_FLOAT);
		Matcher matcher = pattern.matcher(input);
		// 不成功，使用默认值
		return matcher.matches();
	}
	
	/**
	 * 解析单浮点值
	 * @param input
	 * @param defaultValue
	 * @return float
	 */
	public static float splitFloat(String input, float defaultValue) {
		if (input == null) {
			return defaultValue;
		}

		Pattern pattern = Pattern.compile(ConfigParser.REGEX_FLOAT);
		Matcher matcher = pattern.matcher(input);
		// 不成功，使用默认值
		if (!matcher.matches()) {
			return defaultValue;
		}
		// 解析单浮点值
		try {
			return Float.parseFloat(matcher.group(1));
		} catch (NumberFormatException e) {

		} catch (Throwable e){
			
		}
		return defaultValue;
	}

	/** 双浮点表示 **/
	private final static String REGEX_DOUBLE = "^\\s*([0-9]+[\\.0-9]*)\\s*(?i)(?:D|F*)\\s*$";
	
	/**
	 * 判断是浮点语法
	 * @param input 输入语句
	 * @return 匹配返回真，否则假
	 */
	public static boolean isDouble(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		Pattern pattern = Pattern.compile(ConfigParser.REGEX_DOUBLE);
		Matcher matcher = pattern.matcher(input);
		// 不成功，使用默认值
		return matcher.matches();
	}
	
	/**
	 * 解析双浮点值
	 * @param input
	 * @param defaultValue
	 * @return double
	 */
	public static double splitDouble(String input, double defaultValue) {
		if (input == null) {
			return defaultValue;
		}

		Pattern pattern = Pattern.compile(ConfigParser.REGEX_DOUBLE);
		Matcher matcher = pattern.matcher(input);
		// 不成功，使用默认值
		if (!matcher.matches()) {
			return defaultValue;
		}
		// 解析双浮点值
		try {
			return Double.parseDouble(matcher.group(1));
		} catch (NumberFormatException e) {

		} catch (Throwable e){
			
		}
		return defaultValue;
	}

	/**
	 * 解析SOCKET连接类型。分别是：TCP、UDP 两个关键字。如果没有匹配，弹出错误
	 * @param input 输入文本
	 * @param defaultSocket 默认SOCKET类型
	 * @return 返回SOCKET类型
	 */
	public static byte splitSocketFamily(String input, byte defaultSocket) {
		if (input == null) {
			return defaultSocket;
		}

		final String regex = "^\\s*(?i)(TCP|UDP)\\s*$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		// 不匹配使用默认值
		if (!matcher.matches()) {
			return defaultSocket;
		}

		String unit = matcher.group(1);
		if (unit.matches("^\\s*(?i)(TCP)\\s*$")) {
			return SocketTag.TCP;
		} else {
			return SocketTag.UDP;
		}
	}

	/**
	 * 解析转义路径，忽略两侧的空格。<br>
	 * 
	 * 若路径字符串含有转义字符串：“${...}”，如：“${laxcus.site.default}/conf/local.xml”,方法将从系统中提取转义后的字符串，替换到新文本中。如果系统没有匹配的转义值，将抛出异常。
	 * 
	 * @param path 可能含有转义符的路径名
	 * @return 返回一个过滤和转义后的路径文本
	 */
	public static String splitPath(String path) {
		final String regex = "^\\s*(.*?)\\$\\{([\\p{Print}]+?)\\}(.*?)\\s*$";
		// 解析格式，提取其中的系统符号转换为实际系统参数
		Pattern pattern = Pattern.compile(regex);

		do {
			Matcher matcher = pattern.matcher(path);
			if (!matcher.matches()) {
				return path;
			}
			String s1 = matcher.group(1);
			String sys_name = matcher.group(2); // 系统名称
			String s3 = matcher.group(3);

			// 找到系统属性
			String sys_value = System.getProperty(sys_name);
			if (sys_value == null) {
				throw new IllegalValueException("illegal system property '%s'", sys_name);
			}
			path = s1 + sys_value + s3;
		} while(true);
	}
	
	/** 节点地址转义字符格式，包括IP地址，TCP端口，UDP端口 **/
	private final static String REGEX_SITE1 = "^\\s*(.+?)\\$\\{([\\p{Print}]+?)\\}\\:\\$\\{([\\p{Print}]+?)\\}\\_\\$\\{([\\p{Print}]+?)\\}\\s*$";
	/** 节点地址转义字符格式，包括IP地址 **/
	private final static String REGEX_SITE2 = "^\\s*(.+?)\\$\\{([\\p{Print}]+?)\\}(.+?)\\s*$";

	/**
	 * 解析转义节点地址，忽略两侧的空格
	 * 若路径字符串含有转义字符:“${...}”，把它转义成可实际地址格式。转义地址格式：
	 * 1. “call://${laxcus.private.site}:${call.tcp.port}_${call.udp.port}}”
	 * 2. “call://${laxcus.public.ip}:6500_6500”
	 * @param input 输出文本
	 * @return 返回转义后的节点地址字符串
	 */
	public static String splitSite(String input) throws UnknownHostException {
		// 第一种情况
		Pattern pattern = Pattern.compile(ConfigParser.REGEX_SITE1);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String prefix = matcher.group(1);
			// 取出关键字
			String k1 = matcher.group(2);
			String k2 = matcher.group(3);
			String k3 = matcher.group(4);

			// 取出在系统中定义的参数
			String ip = System.getProperty(k1);
			String tcport = System.getProperty(k2);
			String udport = System.getProperty(k3);
			
			if (ip == null) {
				throw new UnknownHostException("illegal address " + k1);
			}
			if (tcport == null) {
				throw new UnknownHostException("illegal port " + k2);
			}
			if (udport == null) {
				throw new UnknownHostException("illegal port " + k3);
			}
			// 转义后的字符串
			return String.format("%s%s:%s_%s", prefix, ip, tcport, udport);
		}

		// 第二种情况
		pattern = Pattern.compile(ConfigParser.REGEX_SITE2);
		matcher = pattern.matcher(input);
		if (matcher.matches()) {
			String prefix = matcher.group(1);
			String middle = matcher.group(2);
			String suffix = matcher.group(3);
			
			// 取出系统中定义参数，转义处理！
			String ip = System.getProperty(middle);
			if (ip == null) {
				throw new UnknownHostException("illegal address " + middle);
			}
			// 解析后的地址格式
			return prefix + ip + suffix;
		}

		// 原样输出
		return input;
	}
	
	/** 节点端口号转义字符格式， **/
	private final static String REGEX_PORT = "^\\s*\\$\\{([\\p{Print}]+?)\\}\\s*$";
	
	/**
	 * 解析端口号
	 * @param input 输入文本
	 * @param defaultPort 默认端口号
	 * @return 返回解析后的端口号。正确的端口号在 0 - 0xffff范围内！
	 */
	public static int splitPort(String input, int defaultPort) {
		// 如果是整数时
		if (ConfigParser.isInteger(input)) {
			return ConfigParser.splitInteger(input, defaultPort);
		}

		// 判断是包含了转义字符的格式
		Pattern pattern = Pattern.compile(ConfigParser.REGEX_PORT);
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			// 进行转义
			String port = System.getProperty(matcher.group(1));
			// 判断是正整数...
			if (ConfigParser.isInteger(port)) {
				return ConfigParser.splitInteger(port, defaultPort);
			}
		}

		return defaultPort;
	}
	
	/** ESL格式颜色 **/
	public final static String ESL_REGEX = "^\\s*(?i)(?:ESL\\s*\\:\\s*\\{)\\s*([0-9]+[\\.0-9]*)\\s*\\,\\s*([0-9]+[\\.0-9]*)\\s*\\,\\s*([0-9]+[\\.0-9]*)\\s*(?:\\})\\s*$";
	
	/**
	 * 判断是ESL格式颜色
	 * @param input 输入语句
	 * @return 返回真或者假
	 */
	public static boolean isESLColor(String input) {
		if (input == null || input.isEmpty()) {
			return false;
		}
		Pattern pattern = Pattern.compile(ConfigParser.ESL_REGEX);
		Matcher matcher = pattern.matcher(input);
		return matcher.matches();
	}
	
	/**
	 * 解析ESL颜色
	 * @param input
	 * @param defaultColor
	 * @return Color
	 */
	public static java.awt.Color splitESLColor(String input, java.awt.Color defaultColor) {
		// 空指针时
		if (input == null || input.isEmpty()) {
			return defaultColor;
		}
		Pattern pattern = Pattern.compile(ConfigParser.ESL_REGEX);
		Matcher matcher = pattern.matcher(input);
		// 不匹配，返回默认值
		if (!matcher.matches()) {
			return defaultColor;
		}

		double H = Double.parseDouble(matcher.group(1));
		double S = Double.parseDouble(matcher.group(2));
		double L = Double.parseDouble(matcher.group(3));
		ESL esl = new ESL(H, S, L);
		RGB rgb = ESLConverter.convert(esl);
		return rgb.toColor();
	}
}