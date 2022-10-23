/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.pool.*;

/**
 * DATA站点的异步调用器管理池。
 * 
 * 负责DATA站点的异步数据调用。
 * 
 * @author scott.liang
 * @version 1.1 7/23/2013
 * @since laxcus 1.0
 */
public class DataInvokerPool extends InvokerPool {

	/** 管理池句柄 **/
	private static DataInvokerPool selfHandle = new DataInvokerPool();

	/**
	 * 构造DATA站点调用器管理池
	 */
	private DataInvokerPool() {
		super();
	}

	/**
	 * 返回调用器管理池的静态句柄
	 * @return
	 */
	public static DataInvokerPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("DataInvokerPool.getInstance");
		// 返回实例
		return DataInvokerPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerPool#launch(com.laxcus.echo.invoke.EchoInvoker)
	 */
	@Override
	public boolean launch(EchoInvoker invoker) {
		return super.defaultLaunch(invoker);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerPool#dispatch(com.laxcus.echo.EchoFlag)
	 */
	@Override
	protected boolean dispatch(EchoFlag flag) {
		return super.defaultDispatch(flag);
	}

}
