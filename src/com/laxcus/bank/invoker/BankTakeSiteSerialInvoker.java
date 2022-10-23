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
 * 向BANK站点申请主机序列号调用器。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class BankTakeSiteSerialInvoker extends BankInvoker {

	/**
	 * 构造向BANK站点申请主机序列号转发命令调用器，指定命令
	 * @param shift 向BANK站点申请主机序列号转发命令
	 */
	public BankTakeSiteSerialInvoker(TakeSiteSerial shift) {
		super(shift);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeSiteSerial getCommand() {
		return (TakeSiteSerial) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeSiteSerial cmd = getCommand();
		Node from = cmd.getSourceSite();
		byte siteFamily = cmd.getSiteFamily();

		// 分别向ACCOUNT/GATE/HASH管理池申请主机编号
		int no = -1;
		
		if (SiteTag.isAccount(siteFamily) && from.isAccount()) {
			no = AccountOnBankPool.getInstance().doSerial(from);
		} else if (SiteTag.isGate(siteFamily) && from.isGate()) {
			no = GateOnBankPool.getInstance().doSerial(from);
		} else if (SiteTag.isHash(siteFamily) && from.isHash()) {
			no = HashOnBankPool.getInstance().doSerial(from);
		}
		
		Logger.debug(this, "launch", "%s no is %d", SiteTag.translate(siteFamily), no);

		// 反馈结果
		TakeSiteSerialProduct product = new TakeSiteSerialProduct(no);
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
