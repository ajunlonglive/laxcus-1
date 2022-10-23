/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.command.account.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 获得组件站点调用器。<br>
 * 命令从TOP站点传来。本地执行去HASH站点查找与签名关联的ACCOUNT站点。
 * 
 * @author scott.liang
 * @version 1.0 7/8/2018
 * @since laxcus 1.0
 */
public class BankTakeSigerSiteInvoker extends BankSeekAccountSiteInvoker {

	/**
	 * 构造获得组件站点调用器，指定命令
	 * @param cmd 获得组件站点
	 */
	public BankTakeSigerSiteInvoker(TakeSigerSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeSigerSite getCommand() {
		return (TakeSigerSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeSigerSite cmd = getCommand();
		List<Siger> users = cmd.list();
		boolean success = seekSites(users, false);
		if (!success) {
			replyProduct(new TakeSigerSiteProduct());
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		List<Seat> seats = replySites();

		TakeSigerSiteProduct product = new TakeSigerSiteProduct();
		for (Seat e : seats) {
			product.add(e.getSiger(), e.getSite());
		}

		boolean success = replyProduct(product);

		return useful(success);
	}

}
