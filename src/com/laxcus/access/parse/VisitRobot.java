/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.access.parse;

/**
 * 远程访问诊断器
 * 
 * @author scott.liang
 * @version 1.0 6/9/2020
 * @since laxcus 1.0
 */
public interface VisitRobot {

	/**
	 * 判断处于在线状态，两个条件：
	 * 1. FRONT节点处于连接状态
	 * 2. 获得了账号资源(Account)实例
	 * @return 返回真或者假
	 */
	boolean isOnline();
}
