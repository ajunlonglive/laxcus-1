/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.home.pool.*;
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
public class HomeCheckUserCostInvoker extends HomeInvoker {

	/**
	 * 构造检测用户消耗资源调用器
	 * @param cmd 检测用户消耗资源
	 */
	public HomeCheckUserCostInvoker(CheckUserCost cmd) {
		super(cmd);
	}
	
	public CheckUserCost getCommand() {
		return (CheckUserCost)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ArrayList<Node> sites = new ArrayList<Node>();

		CheckUserCost cmd = getCommand();

		// HOME CLUSTER
		byte[] types = new byte[] { SiteTag.HOME_SITE, SiteTag.DATA_SITE,
				SiteTag.WORK_SITE, SiteTag.CALL_SITE, SiteTag.BUILD_SITE };
		int count = 0;
		for (int i = 0; i < types.length; i++) {
			if (cmd.hasType(types[i])) {
				count++;
			}
		}
		if (count > 0) {
			NodeSet nodes = LogOnHomePool.getInstance().list();
			if (nodes != null) {
				sites.addAll( nodes.show());
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
					CheckUserCostProduct e = getObject(CheckUserCostProduct.class, index);
					if (e != null) {
						product.add(e);
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
