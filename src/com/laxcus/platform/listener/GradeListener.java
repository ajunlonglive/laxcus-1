/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

/**
 * 级别监听器，属于系统服务
 * 
 * @author scott.liang
 * @version 1.0 3/5/2022
 * @since laxcus 1.0
 */
public interface GradeListener extends PlatformListener {

	/**
	 * 判断是系统管理员
	 * 
	 * @return 返回真或者假
	 */
	boolean isAdministrator();

	/**
	 * 判断是普通注册用户
	 * 
	 * @return 返回真或者假
	 */
	boolean isUser();
	
	/**
	 * 判断处于在线状态
	 * @return 返回真或者假
	 */
	boolean isOnline();
}