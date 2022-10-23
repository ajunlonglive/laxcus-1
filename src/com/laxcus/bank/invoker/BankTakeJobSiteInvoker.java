/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.task.*;

/**
 * 获取工作节点调用器。
 * 转发给TOP节点!
 * 
 * @author scott.liang
 * @version 1.0 3/17/2020
 * @since laxcus 1.0
 */
public class BankTakeJobSiteInvoker extends BankInvoker {

	/**
	 * 构造默认的获取工作节点调用器，指定命令
	 * @param cmd 获取工作节点
	 */
	public BankTakeJobSiteInvoker(TakeJobSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeJobSite getCommand() {
		return (TakeJobSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeJobSite cmd = getCommand();
		boolean success = launchToHub(cmd);
		if (!success) {
			replyFault();
		}
		// 返回结果
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return reflect();
	}

}
