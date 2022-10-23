/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.site.front.*;
import com.laxcus.home.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 构造检测作业节点调用器
 * 
 * @author scott.liang
 * @version 1.0 9/21/2022
 * @since laxcus 1.0
 */
public class HomeCheckJobSiteInvoker extends HomeInvoker {

	/**
	 * 构造构造检测作业节点调用器，指定命令
	 * @param cmd 检测作业节点
	 */
	public HomeCheckJobSiteInvoker(CheckJobSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckJobSite getCommand() {
		return (CheckJobSite)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CheckJobSite cmd = getCommand();
		Siger issuer =cmd.getIssuer();
		
		CheckJobSiteProduct product =new CheckJobSiteProduct();
		
		if (issuer != null) {
			// 取CALL节点
			NodeSet set = CallOnHomePool.getInstance().findSites(issuer);
			if (set != null) {
				product.addAll(set.show());
			}
			// 取DATA节点
			set = DataOnHomePool.getInstance().findSites(issuer);
			if (set != null) {
				product.addAll(set.show());
			}
			// 取WORK节点
			set = WorkOnHomePool.getInstance().findSites(issuer);
			if (set != null) {
				product.addAll(set.show());
			}
			// 取BUILD节点
			set = BuildOnHomePool.getInstance().findSites(issuer);
			if (set != null) {
				product.addAll(set.show());
			}
		}
		
		// 反馈结果
		replyProduct(product);
		
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
