/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import java.util.*;

/**
 * FRONT在线用户分析器
 * 
 * @author scott.liang
 * @version 1.0 10/27/2019
 * @since laxcus 1.0
 */
final class FrontChecker extends TimerTask {
	
	/** FRONT用户管理池 **/
	private FrontOnGatePool frontPool;

	/**
	 * 资源分析器
	 * @param e
	 */
	public FrontChecker(FrontOnGatePool e) {
		frontPool = e;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		frontPool.checkFronts();
	}

}