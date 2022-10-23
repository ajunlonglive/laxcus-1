/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.bank.pool.*;
import com.laxcus.command.access.account.*;
import com.laxcus.site.*;

/**
 *
 * @author scott.liang
 * @version 1.0 7/28/2018
 * @since laxcus 1.0
 */
public class BankTakeAdministratorInvoker extends BankInvoker {

	/**
	 * @param cmd
	 */
	public BankTakeAdministratorInvoker(TakeAdministrator cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeAdministrator getCommand() {
		return (TakeAdministrator) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 结果报告
		TakeAdministratorProduct product = new TakeAdministratorProduct();

		// 判断已经注册到管理池中
		Node slave = getCommandSite();
		boolean success = GateOnBankPool.getInstance().contains(slave);
		if (success) {
			Administrator admin = getLauncher().getAdministrator();
			product.setAdministrator(admin);
		}

		// 反馈结果
		replyProduct(product);

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
