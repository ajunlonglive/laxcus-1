/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.application.factory;

import com.laxcus.application.manage.*;

/**
 * 应用软件包安装工厂
 * 
 * 将一个应用安装到启动菜单和桌面.
 * 
 * @author scott.liang
 * @version 1.0 7/27/2021
 * @since laxcus 1.0
 */
public interface InstallFactory {

	/**
	 * 判断有应用
	 * @param item
	 * @return
	 */
	boolean hasApplication(WRoot item);
	
	/**
	 * 安装软件包
	 * @param item
	 * @return
	 */
	boolean setup(WRoot item);
	
	
}
