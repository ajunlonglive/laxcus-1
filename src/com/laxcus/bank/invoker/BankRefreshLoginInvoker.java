/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.command.site.*;
import com.laxcus.echo.*;
import com.laxcus.bank.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 强制要求指定站点重新注册。<br>
 * 只强制BANK集群下属站点
 * 
 * @author scott.liang
 * @version 1.0 6/26/2018
 * @since laxcus 1.0
 */
public class BankRefreshLoginInvoker extends BankInvoker {

	/** 返回结果 **/
	private RefreshLoginProduct product = new RefreshLoginProduct();

	/**
	 * 构造强制重新注册调用器，指定命令
	 * @param cmd 强制重新注册命令
	 */
	public BankRefreshLoginInvoker(RefreshLogin cmd) {
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
		Node site = cmd.getSourceSite();

		boolean success = false;
		// 判断是来自TOP或者WATCH站点，分别处理
		if (site.isTop()) {
			success = doTop(cmd);
		} else if (site.isWatch()) {
			success = doWatch(cmd);
		}
		// 不成功，拒绝它
		if(!success) {
			replyFault(Major.FAULTED, Minor.REFUSE);
		}
		// 返回结果
		return success;
	}
	
	/**
	 * 来自TOP站点，BANK站点重新注册
	 * @param cmd 强制注册
	 * @return 成功返回真，否则假
	 */
	private boolean doTop(RefreshLogin cmd) {
		// 命令来源地址
		Node node = cmd.getSourceSite();
		// 上级站点地址
		Node hub = getHub();

		// 必须比较一致
		boolean success = (Laxkit.compareTo(node, hub) == 0);
		// 通知重新注册
		if (success) {
			getLauncher().checkin(false);
		}

		// 返回结果
		RefreshLoginProduct product = new RefreshLoginProduct();
		product.add(getLocal(), success);
		success = replyProduct(product);

		return useful(success);
	}
	
	/**
	 * 来自WATCH站点，BANK站点通知下属站点，重新注册到BANK站点
	 * @param cmd 强制注册命令
	 * @return 成功返回真，否则假
	 */
	private boolean doWatch(RefreshLogin cmd) {
		// 必须是WATCH站点，且已经注册
		Node listen = cmd.getSourceSite();
		boolean success = WatchOnBankPool.getInstance().contains(listen);
		// 不成功，拒绝它
		if(!success) {
			return false;
		}

		TreeSet<Node> sites = new TreeSet<Node>();
		if (cmd.isAll()) {
			sites.addAll(AccountOnBankPool.getInstance().detail());
			sites.addAll(HashOnBankPool.getInstance().detail());
			sites.addAll(GateOnBankPool.getInstance().detail());
			sites.addAll(EntranceOnBankPool.getInstance().detail());
			sites.addAll(LogOnBankPool.getInstance().detail());
		} else {
			for (Node site : cmd.list()) {
				// 判断是ACCOUNT/HASH/GATE/ENTRANCE/LOG中的一种
				success = AccountOnBankPool.getInstance().contains(site);
				if (!success) {
					success = HashOnBankPool.getInstance().contains(site);
				}
				if (!success) {
					success = GateOnBankPool.getInstance().contains(site);
				}
				if (!success) {
					success = EntranceOnBankPool.getInstance().contains(site);
				}
				if (!success) {
					success = LogOnBankPool.getInstance().contains(site);
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
			return false;
		}

		// 以容错模式，向下属站点发送命令
		RefreshLogin sub = new RefreshLogin();
		int count = incompleteTo(sites, sub);
		success = (count > 0);
		
		Logger.debug(this, "doWatch", success, "site size:%d, send size:%d", sites.size(), count);

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
