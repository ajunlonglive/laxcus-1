/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

/**
 * 帮助监听器
 * 
 * @author scott.liang
 * @version 1.0 3/5/2022
 * @since laxcus 1.0
 */
public interface HelpListener extends PlatformListener {

	/**
	 * 显示桌面窗口上的帮助信息
	 * @param command 命令...
	 */
	void showHelp(String command);
	
}