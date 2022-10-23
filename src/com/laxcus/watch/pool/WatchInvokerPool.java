/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.watch.invoker.*;

/**
 * WATCH站点的异步调用器管理池。
 * 
 * @author scott.liang
 * @version 1.1 12/2/2012
 * @since laxcus 1.0
 */
public class WatchInvokerPool extends InvokerPool {

	/** WATCH调用器管理池句柄 **/
	private static WatchInvokerPool selfHandle = new WatchInvokerPool();
	
	/**
	 * 初始化WATCH调用器管理池
	 */
	private WatchInvokerPool() {
		super();
	}

	/**
	 * 返回WATCH调用器管理池的静态句柄
	 * @return
	 */
	public static WatchInvokerPool getInstance() {
		return WatchInvokerPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerPool#launch(com.laxcus.echo.invoke.EchoInvoker)
	 */
	@Override
	public boolean launch(EchoInvoker invoker) {
		// 允许调用器继承自WatchInvoker，或者从CustomInvoker派生，和其它一些特例...
		boolean success = (isFrom(invoker.getClass(), WatchInvoker.class) || 
				isFrom(invoker.getClass(), EchoInvoker.class) ||
				isFrom(invoker.getClass(), CommonShiftLoginSiteInvoker.class));
		// 启动工作
		if (success) {
			success = defaultLaunch(invoker);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerPool#dispatch(com.laxcus.echo.EchoFlag)
	 */
	@Override
	protected boolean dispatch(EchoFlag flag) {
		return defaultDispatch(flag);
	}

}