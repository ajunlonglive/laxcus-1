/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.site.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;

/**
 * 强制要求指定站点重新注册。<br>
 * 只强制TOP集群下属站点
 * 
 * @author scott.liang
 * @version 1.0 5/12/2017
 * @since laxcus 1.0
 */
public class TopRefreshLoginInvoker extends TopInvoker {

	/** 返回结果 **/
	private RefreshLoginProduct product = new RefreshLoginProduct();

	/**
	 * 构造强制重新注册调用器，指定命令
	 * @param cmd 强制重新注册命令
	 */
	public TopRefreshLoginInvoker(RefreshLogin cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshLogin getCommand() {
		return (RefreshLogin) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshLogin cmd = getCommand();
		Node listen = cmd.getSourceSite();
		// 必须是WATCH站点，且已经注册
		boolean success = listen.isWatch();
		if(success) {
			success = WatchOnTopPool.getInstance().contains(listen);
		}
		// 不成功，拒绝它
		if(!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		TreeSet<Node> sites = new TreeSet<Node>();

		if (cmd.isAll()) {
			sites.addAll(HomeOnTopPool.getInstance().detail());
			sites.addAll(LogOnTopPool.getInstance().detail());
			sites.addAll(BankOnTopPool.getInstance().detail());
		} else {
			for (Node site : cmd.list()) {
				// 判断是LOG/BANK/HOME三种的一种
				success = LogOnTopPool.getInstance().contains(site);
				if (!success) {
					success = BankOnTopPool.getInstance().contains(site);
				}
				if (!success) {
					success = HomeOnTopPool.getInstance().contains(site);
				}
				// 成功，保存它；否则做为失败记录在案
				if (success) {
					sites.add(site);
				} else {
					product.add(site, false);
				}
			}
		}
		// 空集合退出
		if(sites.isEmpty()) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		// 以容错模式，向下属站点发送命令
		RefreshLogin sub = new RefreshLogin();
		int count = incompleteTo(sites, sub);
		success = (count > 0);
		
		Logger.debug(this, "launch", success, "site size:%d, send size:%d", sites.size(), count);
		
		// 不成功，反馈拒绝
		if(!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
			return false;
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		List<Integer> keys = getEchoKeys();
		try {
			for (int index : keys) {
				// 不成功，记录它
				if (!isSuccessCompleted(index)) {
					Node site = getBufferHub(index);
					product.add(site, false);
					continue;
				}
				RefreshLoginProduct e = getObject(RefreshLoginProduct.class, index);
				product.addAll(e);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 反馈给WATCH站点
		boolean success = replyProduct(product);

		Logger.debug(this, "ending", success, "size is %d", product.size());

		return useful(success);
	}

}
