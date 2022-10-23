/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

/**
 * 异步调用器的“结束”代理。
 * 
 * @author scott.liang
 * @version 1.0 03/20/2013
 * @since laxcus 1.0
 */
public final class EndingTrustor extends InvokerTrustor {

	/**
	 * 构造异步调用器的结束代理，指定所需要的参数
	 * @param pool 调用器管理池
	 * @param invoker 异步调用器
	 */
	public EndingTrustor(InvokerPool pool, EchoInvoker invoker) {
		super(pool, invoker);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerTrustor#process()
	 */
	@Override
	public boolean process() {
		return invoker.ending();
	}

}