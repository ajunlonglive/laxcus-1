/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

/**
 * 异步调用器信使。
 * 向实现接口发出当前调用器通知
 * 
 * @author scott.liang
 * @version 1.0 6/1/2021
 * @since laxcus 1.0
 */
public interface InvokerMessenger {

	/**
	 * 通知接口，调用器启动
	 * 
	 * @param invoker 异步调用器句柄
	 */
	void startInvoker(EchoInvoker invoker);

	/**
	 * 通知接口，调用器停止
	 * 
	 * @param invoker 调用器
	 * @param success 成功或者否
	 */
	void stopInvoker(EchoInvoker invoker, boolean success);

}