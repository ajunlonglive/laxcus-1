/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;

/**
 * GATE站点调用器管理池。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class GateInvokerPool extends InvokerPool {

	/** 异步调用器管理池 **/
	private static GateInvokerPool selfHandle = new GateInvokerPool();

	/**
	 * 构造一个默认和私有的GATE站点调用器管理池。
	 */
	private GateInvokerPool() {
		super();
	}

	/**
	 * 返回GATE站点调用器管理池静态句柄
	 * @return GATE站点调用器管理句柄
	 */
	public static GateInvokerPool getInstance() {
		return GateInvokerPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerPool#launch(com.laxcus.echo.invoke.EchoInvoker)
	 */
	@Override
	public boolean launch(EchoInvoker invoker) {
		return defaultLaunch(invoker);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerPool#dispatch(com.laxcus.echo.EchoFlag)
	 */
	@Override
	protected boolean dispatch(EchoFlag flag) {
		return defaultDispatch(flag);
	}
}