/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.pool.*;

/**
 * 异步调用器管理池。只处理异步命令，限CALL站点使用。
 * 
 * @author scott.liang
 * @version 1.3 05/08/2015
 * @since laxcus 1.0
 */
public final class CallInvokerPool extends InvokerPool {

	/** 管理池句柄 **/
	private static CallInvokerPool selfHandle = new CallInvokerPool();

	/**
	 * 构造异步调用器管理池
	 */
	private CallInvokerPool() {
		super();
	}

	/**
	 * 返回异步调用器管理池的静态句柄
	 * @return
	 */
	public static CallInvokerPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("CallInvokerPool.getInstance");
		// 返回句柄
		return CallInvokerPool.selfHandle;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.InvokerPool#launch(com.laxcus.pool.echo.EchoInvoker)
	 */
	@Override
	public boolean launch(EchoInvoker invoker) {
		return super.defaultLaunch(invoker);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.InvokerPool#dispatch(com.laxcus.echo.EchoFlag)
	 */
	@Override
	protected boolean dispatch(EchoFlag flag) {
		return super.defaultDispatch(flag);
	}

}