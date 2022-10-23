/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.bank.pool.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 获得BANK子站点数目调用器。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class BankTakeBankSubSiteCountInvoker extends BankInvoker {

	/**
	 * 构造获得BANK子站点数目转发命令调用器，指定命令
	 * @param shift 获得BANK子站点数目转发命令
	 */
	public BankTakeBankSubSiteCountInvoker(TakeBankSubSiteCount shift) {
		super(shift);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeBankSubSiteCount getCommand() {
		return (TakeBankSubSiteCount) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeBankSubSiteCount cmd = getCommand();
		byte siteFamily = cmd.getApplyFamily();

		// 根据类型，获得BANK子站点数目
		int size = -1;
		if (SiteTag.isGate(siteFamily)) {
			size = GateOnBankPool.getInstance().size();
		} else if (SiteTag.isHash(siteFamily)) {
			size = HashOnBankPool.getInstance().size();
		} else if (SiteTag.isAccount(siteFamily)) {
			size = AccountOnBankPool.getInstance().size();
		} else if (SiteTag.isEntrance(siteFamily)) {
			size = EntranceOnBankPool.getInstance().size();
		}
		
		Logger.debug(this, "launch", "%s size %d", SiteTag.translate(siteFamily), size);

		// 反馈结果
		TakeBankSubSiteCountProduct product = new TakeBankSubSiteCountProduct(size);
		boolean success = replyProduct(product);

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
