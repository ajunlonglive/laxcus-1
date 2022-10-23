/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp;

import java.util.*;

import com.laxcus.util.*;

/**
 * FIXP密文类型
 * 
 * @author scott.liang
 * @version 1.1 01/12/2013
 * @since laxcus 1.0
 */
public final class CipherTag {

	/** 算法名称列表 */
	
	/** AES算法 **/
	public final static int AES = 1;

	/** DES算法 **/
	public final static int DES = 2;

	/** DES3算法 **/
	public final static int DES3 = 3;

	/** BLOWFISH算法 **/
	public final static int BLOWFISH = 4;

	/** 列表中的最大的数字 **/
	final static int MAX = 4;

	/**
	 * 随机选择一个密文类型
	 * @param rnd 随机数 
	 * @return 返回一个指定的密文类型
	 */
	static int random(Random rnd) {
		// 取最大数字的余数，结果一定小于最大数字
		int who = 0;
		// 取
		do {
			who = rnd.nextInt() % CipherTag.MAX;
		} while (who < 0);

		// 自增1
		who++;
		// 返回随机生成的密文类型
		return who;
	}

	/**
	 * 判断是AES算法
	 * @param who 算法类型
	 * @return 返回真或者假
	 */
	public static boolean isAES(int who) {
		return who == CipherTag.AES;
	}

	/**
	 * 判断是DES算法
	 * @param who 算法类型
	 * @return 返回真或者假
	 */
	public static boolean isDES(int who) {
		return who == CipherTag.DES;
	}

	/**
	 * 判断是DES3算法
	 * @param who 算法类型
	 * @return 返回真或者假
	 */
	public static boolean isDES3(int who) {
		return who == CipherTag.DES3;
	}

	/**
	 * 判断是BLOWFISH算法
	 * @param who 算法类型
	 * @return 返回真或者假
	 */
	public static boolean isBlowfish(int who) {
		return who == CipherTag.BLOWFISH;
	}

	/**
	 * 判断是列表中定义的合法密文类型
	 * @param who 算法类型
	 * @return 返回真或者假
	 */
	public static boolean isCipher(int who) {
		switch (who) {
		case CipherTag.AES:
		case CipherTag.DES:
		case CipherTag.DES3:
		case CipherTag.BLOWFISH:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 将算法的数字描述转为字符串描述
	 * @param who 算法类型
	 * @return 字符串
	 */
	public static String translate(int who) {
		switch (who) {
		case CipherTag.DES:
			return "DES";
		case CipherTag.DES3:
			return "DES3";
		case CipherTag.AES:
			return "AES";
		case CipherTag.BLOWFISH:
			return "BLOWFISH";
		}
		throw new IllegalValueException("illegal algorithm:%d", who);
	}

	/**
	 * 将算法的字符串描述转为数字描述。不匹配弹出IllegalValueException异常。
	 * @param input 输入算法名称
	 * @return 算法类型
	 * @throws IllegalValueException
	 */
	public static int translate(String input) {		
		if (input.matches("^\\s*(?i)(?:DES)\\s*$")) {
			return CipherTag.DES;
		} else if (input.matches("^\\s*(?i)(?:DES3|3DES)\\s*$")) {
			return CipherTag.DES3;
		} else if (input.matches("^\\s*(?i)(?:AES)\\s*$")) {
			return CipherTag.AES;
		} else if (input.matches("^\\s*(?i)(?:BLOWFISH)\\s*$")) {
			return CipherTag.BLOWFISH;
		} 

		throw new IllegalValueException("illegal cipher algorithm %s", input);
	}

}