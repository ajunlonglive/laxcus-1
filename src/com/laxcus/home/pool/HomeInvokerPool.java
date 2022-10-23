/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;

/**
 * HOME站点的“异步调用器”管理池。<br><br>
 * 
 * HOME站点的启动（launch）和分派（dispatch）工作都由线程处理，调用上级默认接口 。<br>
 * 
 * @author scott.liang
 * @version 1.2 3/13/2013
 * @since laxcus 1.0
 */
public final class HomeInvokerPool extends InvokerPool {

	/** 管理池句柄 **/
	private static HomeInvokerPool selfHandle = new HomeInvokerPool();

	/**
	 * 初始化异步调用器管理池
	 */
	private HomeInvokerPool() {
		super();
	}

	/**
	 * 返回异步调用器管理池的静态句柄
	 * @return HomeInvokerPool实例
	 */
	public static HomeInvokerPool getInstance() {
		return HomeInvokerPool.selfHandle;
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
