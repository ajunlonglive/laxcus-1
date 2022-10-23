/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util;

import com.laxcus.util.charset.*;
import com.laxcus.util.classable.*;

/**
 * 半截编码/解码器
 * 
 * @author scott.liang
 * @version 1.0 11/25/2017
 * @since laxcus 1.0
 */
public class Halffer {

	/** 定位符 **/
	private static final char FIXER = 'a';

	/**
	 * 判断字符在范围内
	 * @param c 字符
	 * @return 返回真或者假
	 */
	private static boolean allow(char c) {
		return (Halffer.FIXER <= c && c <= Halffer.FIXER + 0xF);
	}

	/**
	 * 高位编码
	 * @param b 字节
	 * @return 返回这个字节的高位ASCII码
	 */
	private static byte high_encode(byte b) {
		byte e = (byte) ((b >>> 4) & 0xF);
		return (byte) (Halffer.FIXER + e);
	}

	/**
	 * 低位编码
	 * @param b 字节
	 * @return 返回这个字节的低位ASCII码
	 */
	private static byte low_encode(byte b) {
		byte e = (byte) (b & 0xF);
		return (byte) (Halffer.FIXER + e);
	}

	/**
	 * 高位解码
	 * @param c 字符
	 * @return 返回解码后的字节高位值
	 */
	private static byte high_decode(char c) {
		if (!Halffer.allow(c)) {
			throw new IllegalArgumentException("illegal char:" + c);
		}
		return (byte) (((c - Halffer.FIXER) & 0xF) << 4);
	}

	/**
	 * 低位解码
	 * @param c 字符
	 * @return 返回解码后的字节低位值
	 */
	private static byte low_decode(char c) {
		if (!Halffer.allow(c)) {
			throw new IllegalArgumentException("illegal char:" + c);
		}
		return (byte) ((c - Halffer.FIXER) & 0xF);
	}

	/**
	 * 半截编码。<br>
	 * 将字符串转成指定的半截码字符串。
	 * 
	 * @param b 原始字符串
	 * @return 返回编码后的字符串
	 */
	public static String encode(byte[] b) {
		// 如果是空指针或者空字符串，返回空值
		if (b == null || b.length == 0) {
			return "";
		}

		// 对每个字节进行半截编码
		ClassWriter writer = new ClassWriter(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			byte high = Halffer.high_encode(b[i]);
			byte low = Halffer.low_encode(b[i]);
			writer.write(high);
			writer.write(low);
		}
		// 输出结果
		b = writer.effuse();
		return new String(b);
	}

	/**
	 * 半截编码。<br>
	 * 将字符串转成指定的半截码字符串。
	 * 
	 * @param origin 原始字符串
	 * @return 返回编码后的字符串
	 */
	public static String encode(String origin) {
		// 如果是空指针或者空字符串，返回空值
		if (origin == null || origin.isEmpty()) {
			return "";
		}

		// 转成UTF8编码
		byte[] b = new UTF8().encode(origin);
		return Halffer.encode(b);
	}

	//	/**
	//	 * 半截编码。<br>
	//	 * 将字符串转成指定的半截码字符串。
	//	 * 
	//	 * @param origin 原始字符串
	//	 * @return 返回编码后的字符串
	//	 */
	//	public static String encode(String origin) {
	//		// 如果是空指针或者空字符串，返回空值
	//		if (origin == null || origin.isEmpty()) {
	//			return "";
	//		}
	//
	//		// 转成UTF8编码
	//		byte[] b = new UTF8().encode(origin);
	//		// 对每个字节进行半截编码
	//		ClassWriter writer = new ClassWriter(b.length * 2);
	//		for (int i = 0; i < b.length; i++) {
	//			byte high = Halffer.high_encode(b[i]);
	//			byte low = Halffer.low_encode(b[i]);
	//			writer.write(high);
	//			writer.write(low);
	//		}
	//
	//		// 输出结果
	//		b = writer.effuse();
	//		return new String(b);
	//	}

	/**
	 * 半截解码。<br>
	 * 将半截字符串转成原生字符串。
	 * 
	 * @param half 半截字符串
	 * @return 返回解码后的原生字符串。
	 */
	public static byte[] decodeBytes(String half) {
		if (half == null || half.isEmpty()) {
			throw new NullPointerException();
		} else if (half.length() % 2 != 0) {
			throw new IllegalArgumentException("illgal half length:" + half.length());
		}

		// 取出字符，进行解码
		ClassWriter writer = new ClassWriter(half.length() / 2);
		for (int i = 0; i < half.length(); i += 2) {
			byte high = Halffer.high_decode(half.charAt(i));
			byte low = Halffer.low_decode(half.charAt(i + 1));
			byte b = (byte) (high | low); // 合并
			writer.write(b);
		}

		// 解码后输出
		return writer.effuse();
	}

	/**
	 * 半截解码。<br>
	 * 将半截字符串转成原生字符串。
	 * 
	 * @param half 半截字符串
	 * @return 返回解码后的原生字符串。
	 */
	public static String decode(String half) {
		// 解码后输出
		byte[] b = Halffer.decodeBytes(half);
		return new UTF8().decode(b);
	}

	//	/**
	//	 * 半截解码。<br>
	//	 * 将半截字符串转成原生字符串。
	//	 * 
	//	 * @param half 半截字符串
	//	 * @return 返回解码后的原生字符串。
	//	 */
	//	public static String decode(String half) {
	//		if (half == null || half.isEmpty()) {
	//			throw new NullPointerException();
	//		} else if (half.length() % 2 != 0) {
	//			throw new IllegalArgumentException("illgal half length:" + half.length());
	//		}
	//
	//		// 取出字符，进行解码
	//		ClassWriter writer = new ClassWriter(half.length() / 2);
	//		for (int i = 0; i < half.length(); i += 2) {
	//			byte high = Halffer.high_decode(half.charAt(i));
	//			byte low = Halffer.low_decode(half.charAt(i + 1));
	//			byte b = (byte) (high | low); // 合并
	//			writer.write(b);
	//		}
	//
	//		// 解码后输出
	//		byte[] b = writer.effuse();
	//		return new UTF8().decode(b);
	//	}

	//	private static void printAlpha() {
	//		char w = 0x20;
	//		for(; w <= 0x7f; w++) {
	//			System.out.printf("%c  %d  %x\n", w, (int)w, (int)w);
	//		}
	//	}
	//
	//	public static void main(String[] args) {
	//		String line = "岁月";//"UNIX_SYSTEM中华人民共和国天下英雄出我辈一入江湖岁月催";
	//		String e = Halffer.encode(line);
	//		System.out.printf("%d - %s\n", e.length(), e);
	//		e = Halffer.decode(e);
	//		System.out.printf("%d - %s\n", e.length(), e);
	//		System.out.printf("%c - %c\n\n", FIXER, FIXER +15);
	//		Halffer.printAlpha();
	//	}

}