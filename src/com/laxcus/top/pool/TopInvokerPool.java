/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;

/**
 * 异步命令调用器管理池。只限TOP站点使用。<br><br>
 * 
 * 进入管理池的异步命令调用器，在管理池中保存，启动工作(EchoInvoker.launch)由异步命令转发器(EchoClient)去处理。
 * 
 * @author scott.liang
 * @version 1.2 3/13/2013
 * @since laxcus 1.0
 */
public final class TopInvokerPool extends InvokerPool {

	/** 管理池句柄 **/
	private static TopInvokerPool selfHandle = new TopInvokerPool();

	/**
	 * 构造TOP站点的异步命令调用器管理池
	 */
	private TopInvokerPool() {
		super();
	}

	/**
	 * 返回TOP站点异步命令调用器管理池静态句柄
	 * @return
	 */
	public static TopInvokerPool getInstance() {
		return TopInvokerPool.selfHandle;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.pool.echo.InvokerPool#launch(com.laxcus.pool.echo.EchoInvoker)
	 */
	@Override
	public boolean launch(EchoInvoker invoker) {
		return super.defaultLaunch(invoker);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoPool#dispatch(com.laxcus.echo.EchoFlag)
	 */
	@Override
	protected boolean dispatch(EchoFlag flag) {
		return super.defaultDispatch(flag);
	}

}
