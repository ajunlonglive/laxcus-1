/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.site.find.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;

/**
 * 查询站点调用器
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public class TopFindSiteInvoker extends TopInvoker {

	/**
	 * 构造查询站点调用器，指定命令
	 * @param cmd 查询站点命令
	 */
	public TopFindSiteInvoker(FindSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FindSite getCommand() {
		return (FindSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		FindSite cmd = getCommand();
		FindSiteTag tag = cmd.getTag();

		List<Node> nodes = null;
		if (SiteTag.isHome(tag.getFamily())) {
			nodes = HomeOnTopPool.getInstance().detail();
		} else if (SiteTag.isLog(tag.getFamily())) {
			nodes = LogOnTopPool.getInstance().detail();
		} else if (SiteTag.isBank(tag.getFamily())) {
			nodes = BankOnTopPool.getInstance().detail();
		}

		// 查询结果
		FindSiteProduct product = new FindSiteProduct(tag);
		if (nodes != null) {
			product.addSites(nodes);
		}

		Logger.debug(this, "launch", "check %s site, size is %d",
				SiteTag.translate(tag.getFamily()),	product.getSites().size());

		// 发送处理结果
		super.replyProduct(product);

		// 成功完成
		return useful();
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
