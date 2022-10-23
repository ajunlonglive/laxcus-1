/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;

/**
 * HASH站点调用器管理池。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class HashInvokerPool extends InvokerPool {

	/** 异步调用器管理池 **/
	private static HashInvokerPool selfHandle = new HashInvokerPool();

	/**
	 * 构造一个默认和私有的HASH站点调用器管理池。
	 */
	private HashInvokerPool() {
		super();
	}

	/**
	 * 返回HASH站点调用器管理池静态句柄
	 * @return HASH站点调用器管理句柄
	 */
	public static HashInvokerPool getInstance() {
		return HashInvokerPool.selfHandle;
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