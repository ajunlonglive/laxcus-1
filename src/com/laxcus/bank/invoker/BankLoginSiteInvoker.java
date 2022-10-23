/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;
import com.laxcus.command.login.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.bank.pool.*;

/**
 * 注册站点调用器。<br>
 * BANK子站点向BANK站点注册
 * 
 * @author scott.liang
 * @version 1.0 6/26/2018
 * @since laxcus 1.0
 */
public class BankLoginSiteInvoker extends BankInvoker {

	/**
	 * 构造注册站点调用器，指定命令
	 * @param cmd 注册站点命令
	 */
	public BankLoginSiteInvoker(LoginSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public LoginSite getCommand() {
		return (LoginSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		LoginSite cmd = getCommand();
		Site site = cmd.getSite();

		boolean success = false;
		// 注册到指定的管理池中
		if (site.isAccount()) {
			success = AccountOnBankPool.getInstance().add(site);
		} else if (site.isHash()) {
			success = HashOnBankPool.getInstance().add(site);
		} else if (site.isGate()) {
			success = GateOnBankPool.getInstance().add(site);
		} else if (site.isEntrance()) {
			success = EntranceOnBankPool.getInstance().add(site);
		} else if(site.isLog()) {
			success = LogOnBankPool.getInstance().add(site);
		} 
		// 备份BANK站点
		else if(site.isBank()) {
			success = MonitorOnBankPool.getInstance().add(site);
		}
		// WATCH站点
		else if (site.isWatch()) {
			success = WatchOnBankPool.getInstance().add(site);
		}

		Logger.debug(this, "launch", success, "login %s", site);

		// 如果需要反馈时...
		if (cmd.isReply()) {
			LoginSiteProduct product = new LoginSiteProduct(site.getNode(), success);
			replyProduct(product);
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