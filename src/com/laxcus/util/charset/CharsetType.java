/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.charset;

import java.util.*;

import com.laxcus.command.access.table.*;

/**
 *
 * @author Administrator
 * @version 1.0 2019年5月12日
 * @since laxcus 1.0
 */
public class CharsetType {

//	UTF-16
//	UTF-16BE
//	UTF-16LE
//	UTF-32
//	UTF-32BE
//	UTF-32LE
//	UTF-8
	
	/** 无定义 **/
	public final static int NONE = 0;

	/** UTF8格式 **/
	public final static int UTF8 = 1; 

	/** UTF16格式，不默认大小头，系统来定义。**/
	public final static int UTF16 = 2; 
	
	/** UTF16大头格式  **/
	public final static int UTF16_BE = 3;
	
	/** UTF16小头格式 **/
	public final static int UTF16_LE = 4;

	/** UTF32格式 **/
	public final static int UTF32 = 5;
	
	/** UTF32大头格式  **/
	public final static int UTF32_BE = 6;
	
	/** UTF32小头格式 **/
	public final static int UTF32_LE = 7;

	/** GBK格式 **/
	public final static int GBK = 8;
	
	/** GB2312格式 **/
	public final static int GB2312 = 9;
	
	/** GB18030 **/
	public final static int GB18030 = 10;
	
	/**
	 * 输出全部数字描述
	 * @return 字符集的数字描述
	 */
	public static int[] getSymbols() {
		// 这个数组的组合有意为之，尽最大可能检测匹配的字符集
		return new int[] { CharsetType.UTF8, 
				CharsetType.UTF16, CharsetType.UTF16_LE, CharsetType.UTF16_BE,
				CharsetType.UTF32, CharsetType.UTF32_LE, CharsetType.UTF32_BE,
				CharsetType.GB2312, CharsetType.GBK, CharsetType.GB18030 };
	}

	/**
	 * 输出全部字符串描述
	 * @return 字符集的字符串描述
	 */
	public static String[] getStrings() {
		ArrayList<String> a = new ArrayList<String>();

		int[] symbols = CharsetType.getSymbols();
		for (int i = 0; i < symbols.length; i++) {
			String s = CharsetType.translate(symbols[i]);
			a.add(s);
		}

		String[] s = new String[a.size()];
		return a.toArray(s);
	}
	
	/**
	 * 判断是没有定义的编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isNone(int who) {
		return who == CharsetType.NONE;
	}
	
	/**
	 * 判断是UTF8编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isUTF8(int who) {
		return who == CharsetType.UTF8;
	}

	/**
	 * 判断是UTF16编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isUTF16(int who) {
		return who == CharsetType.UTF16;
	}

	/**
	 * 判断是UTF16 BE编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isUTF16_BE(int who) {
		return who == CharsetType.UTF16_BE;
	}

	/**
	 * 判断是UTF16 LE编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isUTF16_LE(int who) {
		return who == CharsetType.UTF16_LE;
	}

	/**
	 * 判断是UTF32 BE编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isUTF32_BE(int who) {
		return who == CharsetType.UTF32_BE;
	}

	/**
	 * 判断是UTF32 LE编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isUTF32_LE(int who) {
		return who == CharsetType.UTF32_LE;
	}

	/**
	 * 判断是UTF32编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isUTF32(int who) {
		return who == CharsetType.UTF32;
	}

	/**
	 * 判断是GBK编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isGBK(int who) {
		return who == CharsetType.GBK;
	}

	/**
	 * 判断是GB2312编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isGB2312(int who) {
		return who == CharsetType.GB2312;
	}

	/**
	 * 判断是GB18030编码
	 * @param who 编码类型
	 * @return 返回真或者假
	 */
	public static boolean isGB18030(int who) {
		return who == CharsetType.GB18030;
	}
	
	/**
	 * 判断是标准的字符集
	 * @param who 字符集类型
	 * @return 支持返回真，否则假
	 */
	public static boolean isCharset(int who) {
		switch (who) {
		case CharsetType.UTF8:
		case CharsetType.UTF16:
		case CharsetType.UTF16_BE:
		case CharsetType.UTF16_LE:
		case CharsetType.UTF32_BE:
		case CharsetType.UTF32_LE:
		case CharsetType.UTF32:
		case CharsetType.GBK:
		case CharsetType.GB2312:
		case CharsetType.GB18030:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * 转义字符集类型
	 * @param who 字符类型
	 * @return 返回文本描述
	 */
	public static String translate(int who) {
		switch (who) {
		case CharsetType.UTF8:
			return "UTF-8";
		case CharsetType.UTF16:
			return "UTF-16";
		case CharsetType.UTF16_BE:
			return "UTF-16BE";
		case CharsetType.UTF16_LE:
			return "UTF-16LE";
		case CharsetType.UTF32:
			return "UTF-32";
		case CharsetType.UTF32_BE:
			return "UTF-32BE";
		case CharsetType.UTF32_LE:
			return "UTF-32LE";
		case CharsetType.GBK:
			return "GBK";
		case CharsetType.GB2312:
			return "GB2312";
		case CharsetType.GB18030:
			return "GB18030";
		}
		return "NONE";
	}
	
	/**
	 * 根据描述字转义字符集类型
	 * @param who 字符串描述
	 * @return 支持的类型
	 */
	public static int translate(String who) {
		// 没有定时
		if (who == null) {
			return CharsetType.NONE;
		}
		// 解析参数
		if (who.matches("^\\s*(?i)(?:UTF-8|UTF8)\\s*$")) {
			return CharsetType.UTF8;
		}
		if (who.matches("^\\s*(?i)(?:UTF-16|UTF16)\\s*$")) {
			return CharsetType.UTF16;
		}
		if (who.matches("^\\s*(?i)(?:UTF-16BE|UTF16BE)\\s*$")) {
			return CharsetType.UTF16_BE;
		}
		if (who.matches("^\\s*(?i)(?:UTF-16LE|UTF16LE)\\s*$")) {
			return CharsetType.UTF16_LE;
		}
		if (who.matches("^\\s*(?i)(?:UTF-32|UTF32)\\s*$")) {
			return CharsetType.UTF32;
		}
		if (who.matches("^\\s*(?i)(?:UTF-32BE|UTF32BE)\\s*$")) {
			return CharsetType.UTF32_BE;
		}
		if (who.matches("^\\s*(?i)(?:UTF-32LE|UTF32LE)\\s*$")) {
			return CharsetType.UTF32_LE;
		}
		if (who.matches("^\\s*(?i)(?:GBK)\\s*$")) {
			return CharsetType.GBK;
		}
		if (who.matches("^\\s*(?i)(?:GB2312)\\s*$")) {
			return CharsetType.GB2312;
		}
		if (who.matches("^\\s*(?i)(?:GB18030)\\s*$")) {
			return CharsetType.GB18030;
		}
		// 输出了！
		return EntityStyle.NONE;
	}
	
}
