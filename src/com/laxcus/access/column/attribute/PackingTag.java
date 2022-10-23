/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.column.attribute;

/**
 * 数据封装标记
 * 
 * @author scott.liang
 * @version 1.0 12/17/2011
 * @since laxcus 1.0
 */
public final class PackingTag {

	/** 支持的压缩算法 **/
	public final static int GZIP = 0x1;
	public final static int ZIP = 0x2;

	/** 支持的加密算法 (des, des3, aes, blowfish) **/
	public final static byte DES = 0x1;
	public final static byte DES3 = 0x2;
	public final static byte AES = 0x3;
	public final static byte BLOWFISH = 0x4;
	
	/**
	 * 判断是GIZP压缩算法
	 * @param input  输入语句
	 * @return  返回真或者假
	 */
	public static boolean isGZIP(String input) {
		return input != null && input.matches("^\\s*(?i)(GZIP)\\s*$");
	}
	
	/**
	 * 判断是ZIP压缩算法
	 * @param input  输入语句
	 * @return  返回真或者假
	 */
	public static boolean isZIP(String input) {
		return input != null && input.matches("^\\s*(?i)(ZIP)\\s*$");
	}
	
	/**
	 * 判断是AES加密算法
	 * @param input  输入语句
	 * @return  返回真或者假
	 */
	public static boolean isAES(String input) {
		return input != null && input.matches("^\\s*(?i)(AES)\\s*$");
	}
	
	/**
	 * 判断是DES加密算法
	 * @param input  输入语句
	 * @return  返回真或者假
	 */
	public static boolean isDES(String input) {
		return input != null && input.matches("^\\s*(?i)(DES)\\s*$");
	}
	
	/**
	 * 判断是3DES加密算法
	 * @param input  输入语句
	 * @return  返回真或者假
	 */
	public static boolean is3DES(String input) {
		return input != null && input.matches("^\\s*(?i)(3DES|DES3)\\s*$");
	}
	
	/**
	 * 判断是BLOWFISH加密算法
	 * @param input  输入语句
	 * @return  返回真或者假
	 */
	public static boolean isBlowfish(String input) {
		return input != null && input.matches("^\\s*(?i)(BLOWFISH)\\s*$");
	}
	
	/**
	 * 将封装类型转义为字符串描述
	 * @param style 封装类型
	 * @return 字符串描述
	 */
	public static String translate(int style) {
		int compress = PackingTag.getCompress(style);
		int encrypt = PackingTag.getEncrypt(style);
		
		// 压缩算法
		String s1 = "NONE";
		switch (compress) {
		case PackingTag.GZIP:
			s1 = "GZIP";
			break;
		case PackingTag.ZIP:
			s1 = "ZIP";
			break;
		}
		// 加密算法
		String s2 = "NONE";
		switch (encrypt) {
		case PackingTag.DES:
			s2 = "DES";
			break;
		case PackingTag.DES3:
			s2 = "DES3";
			break;
		case PackingTag.AES:
			s2 = "AES";
			break;
		case PackingTag.BLOWFISH:
			s2 = "BLOWFISH";
			break;
		}
		// 转为字符串输出
		return String.format("%s/%s", s1, s2);
	}

	/**
	 * 将压缩算法和加密算法标识合并为一个值
	 * @param compress 压缩算法
	 * @param encrypt 加密算法
	 * @return  返回合并的封装类型标记
	 */
	public static int combine(int compress, int encrypt) {
		return ((compress & 0xFF) << 8) | (encrypt & 0xFF);
	}
	
	/**
	 * 从封装类型中转义出压缩算法
	 * @param style 封装类型
	 * @return 压缩算法的数字描述
	 */
	public static int getCompress(int style) {
		return (style >> 8) & 0xFF;
	}

	/**
	 * 从封装类型中转义出加密算法
	 * @param style 封装类型
	 * @return 加密算法的数字描述
	 */
	public static int getEncrypt(int style) {
		return style & 0xFF;
	}

	/**
	 * 判断包含压缩算法
	 * @param style 封装类型
	 * @return 返回真或者假
	 */
	public static boolean isCompress(int style) {
		return getCompress(style) > 0;
	}

	/**
	 * 判断包含加密算法
	 * @param style 封装类型
	 * @return 返回真或者假
	 */
	public static boolean isEncrypt(int style) {
		return getEncrypt(style) > 0;
	}

}