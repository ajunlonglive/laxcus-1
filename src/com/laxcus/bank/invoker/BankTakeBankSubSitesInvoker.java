/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.bank.pool.*;
import com.laxcus.command.site.bank.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.gate.*;
import com.laxcus.site.hash.*;

/**
 * 获得BANK子站点调用器。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class BankTakeBankSubSitesInvoker extends BankInvoker {

	/**
	 * 构造获得BANK子站点转发命令调用器，指定命令
	 * @param cmd 获得BANK子站点转发命令
	 */
	public BankTakeBankSubSitesInvoker(TakeBankSubSites cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeBankSubSites getCommand() {
		return (TakeBankSubSites) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeBankSubSites cmd = getCommand();
		byte applyFamily = cmd.getApplyFamily();

		TakeBankSubSitesProduct product = new TakeBankSubSitesProduct();

		// 分别向ACCOUNT/HASH/GATE管理池获取站点
		if (SiteTag.isAccount(applyFamily)) {
			List<Site> sites = AccountOnBankPool.getInstance().getSites();
			for (Site site : sites) {
				BankSubSiteItem e = new BankSubSiteItem(site.getNode());
				product.add(e);
			}
		} else if (SiteTag.isHash(applyFamily)) {
			List<Site> sites = HashOnBankPool.getInstance().getSites();
			for (Site s : sites) {
				HashSite site = (HashSite) s;
				// 内网地址
				BankSerialSiteItem e = new BankSerialSiteItem(site.getNode(), site.getNo());
				product.add(e);
			}
		} else if (SiteTag.isGate(applyFamily)) {
			List<Site> sites = GateOnBankPool.getInstance().getSites();
			for (Site s : sites) {
				GateSite site = (GateSite) s;
				// 返回内网/公网地址
				BankSerialSiteItem e = new BankSerialSiteItem(site.getPrivate(), site.getPublic(), site.getNo());
				product.add(e);
			}
		}

		Logger.debug(this, "launch", "%s sites is %d", SiteTag.translate(applyFamily), product.size());
		
		// 发送给请求端
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
