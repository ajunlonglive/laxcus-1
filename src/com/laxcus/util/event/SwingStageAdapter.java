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
 * SWING阶段事件适配器
 * 
 * @author scott.liang
 * @version 1.0 10/17/2021
 * @since laxcus 1.0
 */
public class SwingStageAdapter implements SwingStageListener {

	/**
	 * 构造SWING阶段事件适配器
	 */
	public SwingStageAdapter() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.event.SwingStageListener#callReady(java.util.EventObject)
	 */
	@Override
	public void callReady(EventObject e) {

	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.event.SwingStageListener#callLaunch(java.util.EventObject)
	 */
	@Override
	public void callLaunch(EventObject e) {
		
	}

	/* (non-Javadoc)
	 * @see com.laxcus.util.event.SwingStageListener#callExit(java.util.EventObject)
	 */
	@Override
	public void callExit(EventObject e) {
		
	}

}