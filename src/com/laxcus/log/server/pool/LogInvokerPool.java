/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.log.server.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;

/**
 * 日志站点的异步调用器管理池。
 * 
 * @author scott.liang
 * @version 1.0 5/12/2012
 * @since laxcus 1.0
 */
public final class LogInvokerPool extends InvokerPool {

	/** 异步调用器管理池 **/
	private static LogInvokerPool selfHandle = new LogInvokerPool();

	/**
	 * 构造一个默认和私有的日志站点异步调用器管理池。
	 */
	private LogInvokerPool() {
		super();
	}

	/**
	 * 返回日志站点的异步调用器管理池
	 * @return LogInvokerPool实例
	 */
	public static LogInvokerPool getInstance() {
		return LogInvokerPool.selfHandle;
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
