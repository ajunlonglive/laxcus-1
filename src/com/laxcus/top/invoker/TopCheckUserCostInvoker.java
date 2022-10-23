/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.set.*;
import com.laxcus.visit.*;

/**
 * 检测用户消耗资源调用器
 * 
 * @author scott.liang
 * @version 1.0 10/11/2022
 * @since laxcus 1.0
 */
public class TopCheckUserCostInvoker extends TopInvoker {

	/**
	 * 构造检测用户消耗资源调用器
	 * @param cmd 检测用户消耗资源
	 */
	public TopCheckUserCostInvoker(CheckUserCost cmd) {
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
		
		// 检查包含有TOP节点
		if (cmd.hasType(SiteTag.TOP_SITE)) {
			NodeSet nodes = LogOnTopPool.getInstance().list();
			if (nodes != null) {
				sites.addAll(nodes.show());
			}
		}
		
		// 检查包含BANK集群的节点
		byte[] types = new byte[] { SiteTag.BANK_SITE, SiteTag.ENTRANCE_SITE,
				SiteTag.GATE_SITE, SiteTag.HASH_SITE, SiteTag.ACCOUNT_SITE };
		int count = 0;
		for (int i = 0; i < types.length; i++) {
			if (cmd.hasType(types[i])) {
				count++;
			}
		}
		if (count > 0) {
			NodeSet nodes = BankOnTopPool.getInstance().list();
			if (nodes != null) {
				List<Node> list = nodes.show();
				for (Node node : list) {
					if (node.isBank() && node.isManager()) {
						sites.add(node);
					}
				}
			}
		}
		
		// 检查包含有HOME集群的节点
		types = new byte[] { SiteTag.HOME_SITE, SiteTag.DATA_SITE, SiteTag.WORK_SITE, SiteTag.CALL_SITE, SiteTag.BUILD_SITE };
		count = 0;
		for (int i = 0; i < types.length; i++) {
			if (cmd.hasType(types[i])) {
				count++;
			}
		}
		if (count > 0) {
			NodeSet nodes = HomeOnTopPool.getInstance().list();
			if (nodes != null) {
				List<Node> list = nodes.show();
				for (Node node : list) {
					if (node.isHome() && node.isManager()) {
						sites.add(node);
					}
				}
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