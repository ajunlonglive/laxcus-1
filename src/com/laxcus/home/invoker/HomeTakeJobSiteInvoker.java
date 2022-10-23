/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.task.*;
import com.laxcus.home.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;

/**
 * 获取工作节点调用器。
 * 
 * @author scott.liang
 * @version 1.0 3/17/2020
 * @since laxcus 1.0
 */
public class HomeTakeJobSiteInvoker extends HomeInvoker {

	/**
	 * 构造默认的获取工作节点调用器，指定命令
	 * @param cmd 获取工作节点
	 */
	public HomeTakeJobSiteInvoker(TakeJobSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeJobSite getCommand() {
		return (TakeJobSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeJobSite cmd = getCommand();
		Siger username = cmd.getUsername();
		TakeJobSiteProduct product = new TakeJobSiteProduct(username);

		if (username == null) {
			product.addAll(DataOnHomePool.getInstance().detail());
			product.addAll(WorkOnHomePool.getInstance().detail());
			product.addAll(BuildOnHomePool.getInstance().detail());
			product.addAll(CallOnHomePool.getInstance().detail());
		} else {
			// 1. DATA节点
			NodeSet set = DataOnHomePool.getInstance().findSites(username);
			if (set != null) {
				product.addAll(set.show());
			}
			// 2. WORK节点
			set = WorkOnHomePool.getInstance().findSites(username);
			if (set != null) {
				product.addAll(set.show());
			}
			// 3. BUILD节点
			set = BuildOnHomePool.getInstance().findSites(username);
			if (set != null) {
				product.addAll(set.show());
			}
			// 4. CALL节点
			set = CallOnHomePool.getInstance().findSites(username);
			if (set != null) {
				product.addAll(set.show());
			}
		}

		// 反馈结果
		boolean success = replyProduct(product);
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
