/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.tub.servlet.pool;

import com.laxcus.tub.servlet.*;

/**
 * 边缘容器标记执行器
 * 
 * @author scott.liang
 * @version 1.0 6/20/2019
 * @since laxcus 1.0
 */
public interface TubResourceHelper {

	/**
	 * 保存和显示边缘容器标记
	 * @param e 边缘容器标记
	 */
	void addTubTag(TubTag e);

	/**
	 * 删除边缘容器标记，如果有显示，撤销它！
	 * @param e 边缘容器标记
	 */
	void removeTubTag(TubTag e);
}
