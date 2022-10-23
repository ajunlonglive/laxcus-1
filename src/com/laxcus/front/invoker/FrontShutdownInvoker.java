/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import com.laxcus.command.shutdown.*;

/**
 * 远程关闭命令调用器。 <br>
 * 命令来自GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 09/16/2015
 * @since laxcus 1.0
 */
public class FrontShutdownInvoker extends FrontInvoker {

	/**
	 * 构造远程关闭命令调用器，指定命令
	 * @param cmd 构造远程关闭命令
	 */
	public FrontShutdownInvoker(Shutdown cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Shutdown getCommand() {
		return (Shutdown) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 发送给请求端
		ShutdownProduct product = new ShutdownProduct();
		product.add(getLocal(), true);
		boolean success = replyProduct(product);
		// 退出当前线程
		if (success) {
			delay(2000);
			getLauncher().stop();
		}

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
