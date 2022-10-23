/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;

/**
 * ACCOUNT站点调用器管理池。<br>
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public final class AccountInvokerPool extends InvokerPool {

	/** 异步调用器管理池 **/
	private static AccountInvokerPool selfHandle = new AccountInvokerPool();

	/**
	 * 构造一个默认和私有的ACCOUNT站点调用器管理池。
	 */
	private AccountInvokerPool() {
		super();
	}

	/**
	 * 返回ACCOUNT站点调用器管理池静态句柄
	 * @return ACCOUNT站点调用器管理句柄
	 */
	public static AccountInvokerPool getInstance() {
		return AccountInvokerPool.selfHandle;
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
