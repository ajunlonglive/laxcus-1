/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.status;

import com.laxcus.gui.frame.*;

/**
 * 选中内部窗口监听器
 * 
 * @author scott.liang
 * @version 1.0 9/21/2021
 * @since laxcus 1.0
 */
public interface RaySelectFrameListener {

	/**
	 * 选中指定窗口
	 * @param frame
	 */
	void callSelectFrame(LightFrame frame);
}
