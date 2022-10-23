/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.fixp.secure;

import com.laxcus.util.*;

/**
 * 安全检查类型，分为4类：
 * 1. 不检查
 * 2. 只检查地址
 * 3. 只检查密钥
 * 4. 地址/密钥双重验证。
 * 
 * @author scott.liang
 * @version 1.0 11/19/2016
 * @since laxcus 1.0
 */
public final class SecureType {
	
	/** 无效定义 **/
	public final static int INVALID = -1;

	/** 不检查 */
	public final static int NONE = 0;

	/** 客户端地址范围检查 */
	public final static int ADDRESS = 0x1;

	/** 密钥检查 **/
	public final static int CIPHER = 0x2;

	/** 双重检查(即检查客户端地址范围，又检查密钥) */
	public final static int DUPLEX = ADDRESS | CIPHER;
	
	/**
	 * 判断是无效
	 * @param who 安全检查类型
	 * @return 返回真或者假
	 */
	public static boolean isInvalid(int who) {
		return who == SecureType.INVALID;
	}
	
	/**
	 * 判断无校验
	 * @param who 安全检查类型
	 * @return 返回真或者假
	 */
	public static boolean isNone(int who) {
		return who == SecureType.NONE;
	}

	/**
	 * 判断是地址校验
	 * @param who 安全检查类型
	 * @return 返回真或者假
	 */
	public static boolean isAddress(int who) {
		return who > 0 && (who & SecureType.ADDRESS) == SecureType.ADDRESS;
	}

	/**
	 * 判断是密文校验
	 * @param who 安全检查类型
	 * @return 返回真或者假
	 */
	public static boolean isCipher(int who) {
		return who > 0 && (who & SecureType.CIPHER) == SecureType.CIPHER;
	}

	/**
	 * 判断是地址/密文双重校验
	 * @param who 安全检查类型
	 * @return 返回真或者假
	 */
	public static boolean isDuplex(int who) {
		return who == SecureType.DUPLEX;
	}

	/**
	 * 判断是合法的类型
	 * @param who 安全检查类型
	 * @return 返回真或者假
	 */
	public static boolean isFamily(int who) {
		switch (who) {
		case SecureType.NONE:
		case SecureType.ADDRESS:
		case SecureType.CIPHER:
		case SecureType.DUPLEX:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 将安全检查类型翻译为字符串描述
	 * @param who 安全检查类型
	 * @return 字节串
	 */
	public static String translate(int who) {
		switch (who) {
		case SecureType.NONE:
			return "NONE";
		case SecureType.ADDRESS:
			return "ADDRESS";
		case SecureType.CIPHER:
			return "CIPHER";
		case SecureType.DUPLEX:
			return "DUPLEX";
		default:
			return "ILLEGAL SECURE TYPE";
		}
	}
	
	/**
	 * 将安全检查类型翻译为数字描述
	 * @param who 字符串
	 * @return 安全检查类型的整形值
	 */
	public static int translate(String who) {
		if (who.matches("^\\s*(?i)(NONE)\\s*$")) {
			return SecureType.NONE;
		} else if (who.matches("^\\s*(?i)(ADDRESS)\\s*$")) {
			return SecureType.ADDRESS;
		} else if (who.matches("^\\s*(?i)(CIPHER)\\s*$")) {
			return SecureType.CIPHER;
		} else if (who.matches("^\\s*(?i)(DUPLEX)\\s*$")) {
			return SecureType.DUPLEX;
		}
		throw new IllegalValueException("cannot be resolve %s", who);
	}

}