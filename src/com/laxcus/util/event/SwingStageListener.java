/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.util.event;

import java.util.*;

/**
 * SWING阶段事件监听器
 * 
 * @author scott.liang
 * @version 1.0 10/17/2021
 * @since laxcus 1.0
 */
public interface SwingStageListener {

	/**
	 * 进入就绪状态
	 * @param e
	 */
	void callReady(EventObject e);

	/**
	 * 进行执行状态
	 * @param e
	 */
	void callLaunch(EventObject e);

	/**
	 * 进入退出状态
	 * @param e
	 */
	void callExit(EventObject e);
}
