/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop;

import java.awt.*;

import com.laxcus.util.skin.*;

/**
 * 弹出菜单更新器
 * 
 * @author scott.liang
 * @version 1.0 6/3/2021
 * @since laxcus 1.0
 */
public interface DesktopUIUpdater { 
	
	/**
	 * 更新UI
	 * @param token
	 */
	void updateLookAndFeel(SkinToken token);
	
	/**
	 * 更新系统字体
	 * @param font
	 */
	void updateSystemFont(Font font);
}
