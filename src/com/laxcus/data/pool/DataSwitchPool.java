/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.pool;

import com.laxcus.echo.invoke.*;
import com.laxcus.pool.*;

/**
 * DATA节点命令转义管理/分发池
 * 
 * @author scott.liang
 * @version 1.0 4/29/2018
 * @since laxcus 1.0
 */
public class DataSwitchPool extends SwitchPool {

	/** DATA节点命令转义管理/分发池句柄 **/
	private static DataSwitchPool selfHandle = new DataSwitchPool();

	/**
	 * 构造默认和私有的DATA节点命令转义管理/分发池
	 */
	private DataSwitchPool() {
		super();
	}

	/**
	 * 输出DATA节点命令转义管理/分发池句柄。
	 * 
	 * @return DATA节点命令转义管理/分发池
	 */
	public static DataSwitchPool getInstance() {
		// 安全检查
		VirtualPool.check("getInstance");
		// 输出句柄
		return DataSwitchPool.selfHandle;
	}
}