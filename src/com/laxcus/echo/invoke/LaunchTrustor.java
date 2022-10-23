/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoke;

/**
 * 异步调用器的“启动”代理。<br><br>
 * 
 * 异步调用器启动代理是在线程（LaunchTrustor.processs）中调用异步调用器的启动方法（EchoInvoker.launch）。
 * 这样做的目的，当“CommandPool.dispatch”向“InvokerPool.launch”转交EchoInvoker时，
 * 不在"InvokerPool.launch"中执行“EchoInvoker.launch”，也就不会给“CommandPool.dispatch”造成阻塞。<br><br>
 * 
 * LaunchTrustor在线程中调用“EchoInvoker.launch”，需要判断服务端的受理结果。
 * 如果服务器不接受，要把EchoInvoker从InvokerPool中删除，并且发送一个错误应答给命令请求端。
 * 在“EchoInvoker.launch”处理完成后，LaunchTrustor要退出线程。
 * 
 * @author scott.liang
 * @version 1.0 03/20/2013
 * @since laxcus 1.0
 */
public final class LaunchTrustor extends InvokerTrustor {

	/**
	 * 构造异步调用器启动代理，指定调用器管理池和异步调用器。
	 * @param pool 调用器管理池
	 * @param invoker 异步调用器
	 */
	public LaunchTrustor(InvokerPool pool, EchoInvoker invoker) {
		super(pool, invoker);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.InvokerTrustor#process()
	 */
	@Override
	public boolean process() {
		return invoker.launch();
	}

}