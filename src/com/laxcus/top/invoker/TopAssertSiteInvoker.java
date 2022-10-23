/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;

/**
 * 查询站点存在命令调用器
 * 命令由TOP的子站点发出，TOP判断后会出一个响应
 * 
 * @author scott.liang
 * @version 1.0 7/12/2013
 * @since laxcus 1.0
 */
public class TopAssertSiteInvoker extends TopInvoker {

	/**
	 * 构造查询站点存在命令调用器，指定命令
	 * @param cmd 查询站点存在命令
	 */
	public TopAssertSiteInvoker(AssertSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AssertSite getCommand() {
		return (AssertSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AssertSite cmd = getCommand();
		Node node = cmd.getSite();

		boolean success = LogOnTopPool.getInstance().contains(node);
		if (!success) {
			success = BankOnTopPool.getInstance().contains(node);
		}
		if (!success) {
			success = HomeOnTopPool.getInstance().contains(node);
		}
		if (!success) {
			success = WatchOnTopPool.getInstance().contains(node);
		}
		if (!success) {
			success = MonitorOnTopPool.getInstance().contains(node);
		}

		// 发送处理结果给请求端
		AssertSiteProduct product = new AssertSiteProduct(node, success);
		replyProduct(product);

		Logger.debug(this, "launch", success, "site %s", node);

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
