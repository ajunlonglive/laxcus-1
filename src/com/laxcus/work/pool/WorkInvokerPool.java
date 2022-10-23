/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.pool;

import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.pool.*;

/**
 * WORK站点调用器管理池 <br><br>
 * 
 * 工作内容：<br>
 * 1. 接受来自WorkCommandPool分派的异步调用器和启动它(EchoInvoker.launch)。<br>
 * 2. 异步调用器发出的异步操作后，这里进行接收，然后调用回收处理(EchoInvoker.ending)。<br><br>
 * 
 * WORK站点的异步调用器全部采用线程处理。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2013
 * @since laxcus 1.0
 */
public class WorkInvokerPool extends InvokerPool {

	/** WORK站点调用器管理池句柄 **/
	private static WorkInvokerPool selfHandle = new WorkInvokerPool();
	
	/**
	 * 初始化WORK站点调用器管理池
	 */
	private WorkInvokerPool() {
		super();
	}

	/**
	 * 返回WORK站点调用器管理池的静态句柄
	 * @return 返回句柄
	 */
	public static WorkInvokerPool getInstance() {
		// 对调用者进行安全检查，如果是快捷组件或者分布任务组件，将禁止他！
		VirtualPool.check("WorkInvokerPool.getInstance");
		// 返回句柄
		return WorkInvokerPool.selfHandle;
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