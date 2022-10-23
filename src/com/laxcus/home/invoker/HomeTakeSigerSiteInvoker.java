/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.account.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 获得ARCHIVE站点地址命令调用器。
 * 获取HOME站点上的ARCHIVE站点地址
 * 
 * @author scott.liang
 * @version 1.1 7/28/2018
 * @since laxcus 1.0
 */
public class HomeTakeSigerSiteInvoker extends HomeInvoker {

	/**
	 * 构造获得ARCHIVE站点地址命令调用器，指定命令
	 * @param cmd 获得ARCHIVE站点地址命令
	 */
	public HomeTakeSigerSiteInvoker(TakeSigerSite cmd) {
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
		List<Siger> sigers = cmd.list();

		TakeSigerSiteProduct product = new TakeSigerSiteProduct();
		
		for (Siger siger : sigers) {
			// 判断用户签名是允许的
			boolean success = StaffOnHomePool.getInstance().allow(siger);
			if (!success) {
				continue;
			}
			
			List<Node> sites = AccountOnCommonPool.getInstance().findSites(siger);
			if (sites == null) {
				continue;
			}
			// 保存
			for (Node node : sites) {
				product.add(siger, node);
			}
		}

		// 返回报告
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "size is %d", product.size());

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
