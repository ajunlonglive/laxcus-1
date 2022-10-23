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
import com.laxcus.site.*;

/**
 * 撤销HASH站点到GATE站点调用器
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class BankShiftDropHashSiteInvoker extends BankInvoker {

	/**
	 * 构造撤销HASH站点到GATE站点，指定命令
	 * @param shift 撤销HASH站点到GATE站点转发命令
	 */
	public BankShiftDropHashSiteInvoker(ShiftDropHashSite shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftDropHashSite getCommand() {
		return (ShiftDropHashSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftDropHashSite shift = getCommand();

		// 找到全部GATE节点
		List<Node> sites = GateOnBankPool.getInstance().detail();

		// 被注销的HASH节点推送给所有GATE站点
		boolean success = (sites.size() > 0);
		if (success) {
			int count = directTo(sites, shift.getCommand());
			success = (count > 0);
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