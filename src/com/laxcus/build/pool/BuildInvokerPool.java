/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.pool.*;

/**
 * 异步调用器管理池。<br>
 * 
 * BUILD站点上的异步处理全部采用线程方式。
 * 
 * @author scott.liang
 * @version 1.1 8/14/2013
 * @since laxcus 1.0
 */
public class BuildInvokerPool extends InvokerPool {

	/** 管理池句柄 **/
	private static BuildInvokerPool selfHandle = new BuildInvokerPool();
	
	/**
	 * 构造调用器管理池
	 */
	private BuildInvokerPool() {
		super();
	}

	/**
	 * 返回调用器管理池的静态句柄
	 * @return 调用器管理池实例
	 */
	public static BuildInvokerPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("BuildInvokerPool.getInstance");
		// 返回句柄
		return BuildInvokerPool.selfHandle;
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
