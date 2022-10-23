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
import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 检测用户消耗资源调用器
 * 
 * @author scott.liang
 * @version 1.0 10/11/2022
 * @since laxcus 1.0
 */
public class BankCheckUserCostInvoker extends BankInvoker {

	/**
	 * 构造检测用户消耗资源调用器
	 * @param cmd 检测用户消耗资源
	 */
	public BankCheckUserCostInvoker(CheckUserCost cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckUserCost getCommand() {
		return (CheckUserCost) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ArrayList<Node> sites = new ArrayList<Node>();

		CheckUserCost cmd = getCommand();

		// BANK CLUSTER
		byte[] types = new byte[] { SiteTag.BANK_SITE, SiteTag.ENTRANCE_SITE,
				SiteTag.GATE_SITE, SiteTag.HASH_SITE, SiteTag.ACCOUNT_SITE };
		int count = 0;
		for (int i = 0; i < types.length; i++) {
			if (cmd.hasType(types[i])) {
				count++;
			}
		}
		if (count > 0) {
			NodeSet nodes = LogOnBankPool.getInstance().list();
			if (nodes != null) {
				sites.addAll(nodes.show());
			}
		}

		// 没有退出
		if (sites.isEmpty()) {
			replyProduct(new CheckUserCostProduct());
			return useful(false);
		}
		
		// 容错发送
		count = incompleteTo(sites, cmd);
		boolean success = (count > 0);
		if (!success) {
			replyProduct(new CheckUserCostProduct());
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CheckUserCostProduct product = new CheckUserCostProduct();

		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					CheckUserCostProduct sub = getObject(CheckUserCostProduct.class, index);
					if (sub != null) {
						product.add(sub);
					}
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 返回结果
		boolean success = replyProduct(product);

		return useful(success);
	}

}