/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.task;

/**
 * LAXCUS分布计算尾段代理，是PUT/END/NEAR的父类接口，部署在FRONT节点。
 * 
 * @author scott.liang
 * @version 1.0 9/20/2019
 * @since laxcus 1.0
 */
public interface TailTrustor {

	/**
	 * 判断是图形桌面
	 * @return 返回真或者假
	 */
	boolean isDesktop();
	
	/**
	 * 判断是绑定到用户应用、无操作界面的驱动程序站点
	 * @return 返回真或者假
	 */
	boolean isDriver();

	/**
	 * 判断是字符界面的控制台站点
	 * @return 返回真或者假
	 */
	boolean isConsole();

	/**
	 * 判断是图形界面的终端站点
	 * @return 返回真或者假
	 */
	boolean isTerminal();
	
	/**
	 * 判断是用于边缘计算的服务端节点（在后台运行）
	 * @return 返回真或者假
	 */
	boolean isEdge(); 
	
	/**
	 * 判断是被第三方开发，基于FRONT节点的应用软件
	 * @return 返回真或者假
	 */
	boolean isApplication();
	
	
}