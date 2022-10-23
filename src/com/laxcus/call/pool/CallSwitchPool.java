/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import com.laxcus.echo.invoke.*;
import com.laxcus.pool.*;

/**
 * CALL节点命令转义管理/分发池
 * 
 * @author scott.liang
 * @version 1.0 4/29/2018
 * @since laxcus 1.0
 */
public class CallSwitchPool extends SwitchPool {

	/** CALL节点命令转义管理/分发池句柄 **/
	private static CallSwitchPool selfHandle = new CallSwitchPool();

	/**
	 * 构造默认和私有的CALL节点命令转义管理/分发池
	 */
	private CallSwitchPool() {
		super();
	}

	/**
	 * 输出CALL节点命令转义管理/分发池句柄。
	 * 
	 * @return CALL节点命令转义管理/分发池
	 */
	public static CallSwitchPool getInstance() {
		// 安全检查
		VirtualPool.check("getInstance");
		// 输出句柄
		return CallSwitchPool.selfHandle;
	}
}