/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

import com.laxcus.util.*;

/**
 * 用户签名接口。<br><br>
 * 在FrontLauncher实现，绑定LocalTaskPool，判断用户当前状态。
 * 
 * @author scott.liang
 * @version 1.0 6/20/2020
 * @since laxcus 1.0
 */
public interface UserListener {

	/**
	 * 返回用户签名，如果是管理员，返回空指针
	 * 
	 * @return Siger实例或者空指针
	 */
	Siger getIssuer();

	/**
	 * 判断是离线状态
	 * 
	 * @return 返回真或者假
	 */
	boolean isOffline();

	/**
	 * 判断是管理员
	 * 
	 * @return 返回真或者假
	 */
	boolean isAdministrator();

	/**
	 * 判断是普通用户
	 * 
	 * @return 返回真或者假
	 */
	boolean isUser();
}