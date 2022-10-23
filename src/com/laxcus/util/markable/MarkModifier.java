/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.markable;

import java.lang.reflect.*;

/**
 * 标记化修饰符
 * 
 * @author scott.liang
 * @version 1.0 10/18/2017
 * @since laxcus 1.0
 */
public class MarkModifier {

	/**
	 * 判断修饰符是标记化允许的
	 * @param mod 模值
	 * @return 返回真或者假
	 */
	public static boolean allow(int mod) {
		// 以下是不允许的
		if (Modifier.isAbstract(mod)) {
			return false;
		} else if (Modifier.isFinal(mod)) {
			return false;
		} else if (Modifier.isNative(mod)) {
			return false;
		} else if (Modifier.isInterface(mod)) {
			return false;
		} else if (Modifier.isSynchronized(mod)) {
			return false;
		} else if (Modifier.isTransient(mod)) {
			return false;
		}

		// 允许
		return true;
	}

	/**
	 * 判断参数域是标记化允许的
	 * @param field 参数域
	 * @return 返回真或者假
	 */
	public static boolean allow(Field field) {
		int mod = field.getModifiers();
		return MarkModifier.allow(mod);
	}

}