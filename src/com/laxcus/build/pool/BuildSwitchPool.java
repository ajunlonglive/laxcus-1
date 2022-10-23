/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.pool;

import com.laxcus.echo.invoke.*;
import com.laxcus.pool.*;

/**
 * BUILD节点命令转义管理/分发池
 * 
 * @author scott.liang
 * @version 1.0 4/29/2018
 * @since laxcus 1.0
 */
public class BuildSwitchPool extends SwitchPool {

	/** BUILD节点命令转义管理/分发池句柄 **/
	private static BuildSwitchPool selfHandle = new BuildSwitchPool();

	/**
	 * 构造默认和私有的BUILD节点命令转义管理/分发池
	 */
	private BuildSwitchPool() {
		super();
	}

	/**
	 * 输出BUILD节点命令转义管理/分发池句柄。
	 * 
	 * @return BUILD节点命令转义管理/分发池
	 */
	public static BuildSwitchPool getInstance() {
		// 安全检查
		VirtualPool.check("getInstance");
		// 输出句柄
		return BuildSwitchPool.selfHandle;
	}
}