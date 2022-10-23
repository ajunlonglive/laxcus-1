/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.site.*;
import com.laxcus.bank.pool.*;
import com.laxcus.command.site.watch.*;

/**
 * 站点不足提示调用器。
 * 命令来自BANK节点下属的GATE，BANK起中继作用，它转发给WATCH节点
 * 
 * @author scott.liang
 * @version 1.0 6/1/2019
 * @since laxcus 1.0
 */
public class BankSiteMissingInvoker extends BankInvoker {

	/**
	 * 构造站点不足调用器，指定命令
	 * @param cmd 站点不足调用器
	 */
	public BankSiteMissingInvoker(SiteMissing cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SiteMissing getCommand() {
		return (SiteMissing) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		List<Node> slaves = WatchOnBankPool.getInstance().detail();

		boolean success = (slaves.size() > 0);
		if (success) {
			SiteMissing sub = getCommand();
			success = launchTo(slaves, sub);
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
