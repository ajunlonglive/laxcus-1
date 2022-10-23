/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.refer.*;

/**
 * 构造刷新资源引用调用器。<br>
 * BANK站点将命令投递给TOP站点。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class BankRefreshReferInvoker extends BankInvoker {

	/**
	 * 构造刷新资源引用调用器，指定命令
	 * @param cmd 刷新资源引用
	 */
	public BankRefreshReferInvoker(RefreshRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshRefer getCommand() {
		return (RefreshRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshRefer cmd = getCommand();
		// 发送到TOP站点，不需要回应
		boolean success = directToHub(cmd);
		// 退出
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
