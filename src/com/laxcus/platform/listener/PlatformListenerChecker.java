/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

import com.laxcus.util.*;

/**
 * 检查器
 * 
 * @author scott.liang
 * @version 1.0 3/5/2022
 * @since laxcus 1.0
 */
public class PlatformListenerChecker {
	
	/** 系统监听器 **/
	private static Class<?>[] systemListeners = new Class<?>[] {
			CommandDispatcher.class, CommandParser.class, DatabaseListener.class, WareListener.class,
			FrontListener.class, DesktopListener.class, GradeListener.class,
			HelpListener.class, SoundListener.class, WatchListener.class };

	/**
	 * 判断当前监听器在规定范围内，并且属于系统级的监听器
	 * @param o 对象实例
	 * @return 返回真或者假
	 */
	public static boolean isSystemListener(Object o) {
		// 判断是系统接口
		for (int i = 0; i < systemListeners.length; i++) {
			// 判断在规定的系统接口范围内
			boolean success = Laxkit.isInterfaceFrom(o, systemListeners[i]);
			if (success) {
				return true;
			}
		}
		// 不成立，返回假
		return false;
	}

	/**
	 * 判断当前监听器在规定范围内，并且属于用户级的监听器
	 * @param o 对象实例
	 * @return 返回真或者假
	 */
	public static boolean isUserListener(Object o) {
		// 判断属于系统接口
		for (int i = 0; i < systemListeners.length; i++) {
			// 判断在规定的系统接口范围内，返回假
			boolean success = Laxkit.isInterfaceFrom(o, systemListeners[i]);
			if (success) {
				return false;
			}
		}
		// 不是系统监听接口，返回真
		return true;
	}

}