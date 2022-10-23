/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;

/**
 * 打印站点检测目录调用器
 * 
 * @author scott.liang
 * @version 1.0 8/19/2019
 * @since laxcus 1.0
 */
public class SubCheckSitePathInvoker extends CommonCheckSitePathInvoker {

	/**
	 * 构造打印站点检测目录调用器，指定命令
	 * @param cmd 打印站点检测目录命令
	 */
	public SubCheckSitePathInvoker(CheckSitePath cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckSitePath getCommand() {
		return (CheckSitePath) super.getCommand();
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		CheckSitePath cmd = getCommand();
	//		Node from = cmd.getSourceSite();
	//		Node hub = getHub();
	//
	//		// 命令必须来自管理站点，且要求反馈
	//		boolean success = (Laxkit.compareTo(from, hub) == 0 && cmd.isReply());
	//
	//		// 要求反馈结果时...
	//		if (success) {
	//			CheckSitePathProduct product = new CheckSitePathProduct();
	//			CheckSitePathItem item = pickup();
	//			product.add(item);
	//			replyProduct(product);
	//		}
	//
	//		return useful();
	//	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckSitePathItem item = pickup();
		CheckSitePathProduct product = new CheckSitePathProduct();
		product.add(item);
		replyProduct(product);
		
		Logger.debug(this, "launch", "%s", item.toString());

		// 退出！
		return useful(item.isSuccessful());
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