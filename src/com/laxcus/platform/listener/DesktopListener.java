/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

import com.laxcus.gui.*;

/**
 * 桌面监听器，执行与桌面有关的工作，包括打开文件、设置壁纸之类
 * 
 * @author scott.liang
 * @version 1.0 1/27/2022
 * @since laxcus 1.0
 */
public interface DesktopListener extends PlatformListener {

	/** 
	 * 根据传入对象，启动对应的应用，打开对象实例
	 * @param o 目前支持一个或者数组的SRL/File对象
	 */
	void open(Object o);

	/**
	 * 设置桌面壁纸
	 * @param o 支持PNT/JPEG/GIF图片的File对象，或者是Image实例本身
	 * @param layout 总局类型
	 */
	boolean setWallPaper(Object o, int layout);
	
	/**
	 * 窗口全屏或者否
	 * @param form 窗口实例
	 * @param full 全屏幕或者否
	 * @return 成功返回真，否则假
	 */
	boolean setFullScreen(LightForm form, boolean full);
}