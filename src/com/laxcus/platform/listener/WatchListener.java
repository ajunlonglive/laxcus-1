/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform.listener;

/**
 * WATCH系统资源监听器。<br><br>
 * 这是一个系统级的监听器，在RayWindows.initDesktop方法里注册。<br>
 * 用户窗口获得句柄后，提取WATCH节点的系统资源数据。<br>
 * 
 * @author scott.liang
 * @version 1.0 3/9/2022
 * @since laxcus 1.0
 */
public interface WatchListener extends PlatformListener {

	/**
	 * 清除全部资源记录
	 */
	void clear();
	
}
