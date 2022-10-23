/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.login.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;

/**
 * 注册站点调用器。<br>
 * TOP子站点向TOP站点注册
 * 
 * @author scott.liang
 * @version 1.0 12/03/2017
 * @since laxcus 1.0
 */
public class TopLoginSiteInvoker extends TopInvoker {

	/**
	 * 构造注册站点调用器，指定命令
	 * @param cmd 注册站点命令
	 */
	public TopLoginSiteInvoker(LoginSite cmd) {
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
		// 注册到指定节点中
		if (site.isHome()) {
			success = HomeOnTopPool.getInstance().add(site);
		} else if (site.isBank()) {
			success = BankOnTopPool.getInstance().add(site);
		} else if (site.isLog()) {
			success = LogOnTopPool.getInstance().add(site);
		} else if (site.isTop()) {
			success = MonitorOnTopPool.getInstance().add(site);
		} else if (site.isWatch()) {
			success = WatchOnTopPool.getInstance().add(site);
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
