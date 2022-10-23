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
 * 安全管理模式 <br><br>
 * 
 * 说明一个RSA密钥令牌的应用范围，分为“公共”和“指定”两种模式。<br>
 * “指定”模式是在配置文件中定义一组IP地址段，在这个地址范围之外的连接都是无效的。<br>
 * “公共”模式适用于“指定”之外的所有IP地址段。<br>
 * 
 * @author scott.liang
 * @version 1.0 11/19/2016
 * @since laxcus 1.0
 */
public class SecureMode {

	/** 公共模式 */
	public static final int COMMON = 1;

	/** 指定模式 */
	public static final int SPECIAL = 2;

	/**
	 * 将安全管理模式翻译为字符串描述
	 * @param who 安全管理模式
	 * @return 安全管理模式的字符串
	 */
	public static String translate(int who) {
		switch (who) {
		case SecureMode.COMMON:
			return "COMMON";
		case SecureMode.SPECIAL:
			return "SPECIAL";
		default:
			return "ILLEGAL SECURE MODEL";
		}
	}

	/**
	 * 将安全管理模式翻译为数字描述
	 * @param who 字符串描述
	 * @return 安全管理模式的整形值
	 */
	public static int translate(String who) {
		if (who.matches("^\\s*(?i)(DEFAULT|COMMON)\\s*$")) {
			return SecureMode.COMMON;
		} else if (who.matches("^\\s*(?i)(SPECIAL)\\s*$")) {
			return SecureMode.SPECIAL;
		}
		throw new IllegalValueException("cannot be resolve %s", who);
	}

	/**
	 * 判断是公用模式
	 * @param who 安全管理模式
	 * @return 返回真或者假
	 */
	public static boolean isCommon(int who) {
		return who == SecureMode.COMMON;
	}

	/**
	 * 判断是特殊模式
	 * @param who 安全管理模式
	 * @return 返回真或者假
	 */
	public static boolean isSpecial(int who) {
		return who == SecureMode.SPECIAL;
	}

	/**
	 * 判断是有效模式
	 * @param who 安全管理模式
	 * @return 返回真或者假
	 */
	public static boolean isMode(int who) {
		switch (who) {
		case SecureMode.COMMON:
		case SecureMode.SPECIAL:
			return true;
		default:
			return false;
		}
	}

}