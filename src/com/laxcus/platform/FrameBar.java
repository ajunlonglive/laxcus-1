/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.platform;

import com.laxcus.gui.frame.*;

/**
 * 应用条
 * 
 * @author scott.liang
 * @version 1.0 9/19/2021
 * @since laxcus 1.0
 */
public interface FrameBar {
	
	/**
	 * 窗口激活
	 * @param frame
	 */
	public void activate(LightFrame frame);
	
	/**
	 * 取消激活
	 * @param frame
	 */
	public void deactivate(LightFrame frame);

	/**
	 * 在桌面上注册窗口 发生在绑定窗口的时候
	 * 
	 * @param frame
	 */
	public void register(LightFrame frame);

	/**
	 * 从桌面上注销窗口 发生在释放窗口的时候
	 * 
	 * @param frame
	 */
	public void unregister(LightFrame frame);

}
