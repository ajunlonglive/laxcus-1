/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.site.watch.*;
import com.laxcus.watch.component.*;
import com.laxcus.watch.pool.*;

/**
 * 站点运行状态调用器
 * 
 * @author scott.liang
 * @version 1.0 4/13/2018
 * @since laxcus 1.0
 */
public class WatchSiteRuntimeInvoker extends WatchInvoker {

	/**
	 * 构造站点运行状态，指定命令
	 * @param cmd 站点运行状态
	 */
	public WatchSiteRuntimeInvoker(SiteRuntime cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SiteRuntime getCommand() {
		return (SiteRuntime) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SiteRuntime cmd = getCommand();

		// 判断处于登录状态，并且节点地址有效
		boolean success = (getLauncher().isLogined() && SiteOnWatchPool
				.getInstance().contains(cmd.getNode()));

		// 交给WATCH图形界面显示
		if (success) {
			WatchMixedPanel display = getDisplayPanel();
			display.showRuntime(cmd);
			
			// 保存节点运行状态
			SiteRuntimeBasket.getInstance().pushRuntime(cmd);
			
			// 调整运行时参数
			getLauncher().modify(cmd);
		}
		
		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}