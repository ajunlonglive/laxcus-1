/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.pool;

import com.laxcus.echo.invoke.*;
import com.laxcus.pool.*;

/**
 * WORK节点命令转义管理/分发池
 * 
 * @author scott.liang
 * @version 1.0 4/29/2018
 * @since laxcus 1.0
 */
public class WorkSwitchPool extends SwitchPool {

	/** WORK节点命令转义管理/分发池句柄 **/
	private static WorkSwitchPool selfHandle = new WorkSwitchPool();

	/**
	 * 构造默认和私有的WORK节点命令转义管理/分发池
	 */
	private WorkSwitchPool() {
		super();
	}

	/**
	 * 输出WORK节点命令转义管理/分发池句柄。
	 * 
	 * @return WORK节点命令转义管理/分发池
	 */
	public static WorkSwitchPool getInstance() {
		// 安全检查
		VirtualPool.check("getInstance");
		// 输出句柄
		return WorkSwitchPool.selfHandle;
	}
}