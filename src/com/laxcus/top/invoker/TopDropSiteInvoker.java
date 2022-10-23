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
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;

/**
 * 推送注销站点调用器
 * 
 * @author scott.liang
 * @version 1.0 12/11/2011
 * @since laxcus 1.0
 */
public class TopDropSiteInvoker extends TopInvoker {

	/**
	 * 构造推送注销站点调用器，指定命令
	 * @param cmd 推送注销站点
	 */
	public TopDropSiteInvoker(DropSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropSite getCommand() {
		return (DropSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropSite cmd = getCommand();
		// 判断是监视站点
		boolean success = isMonitor();
		// 保存这个站点
		if (success) {
			Node node = cmd.getSite();
			success = getLauncher().drop(node);
		}
		
		// 命令推送给WATCH节点，因为TOP.WATCH节点保存有BANK/HOME子域集群下属站点的用户签名
		pushToWatch(cmd);

		Logger.debug(this, "launch", success, "drop %s", cmd.getSite());

		return useful(success);
	}
	
	/**
	 * 推送给WATCH节点
	 * @param cmd
	 */
	private void pushToWatch(DropSite cmd) {
		List<Node> slaves = WatchOnTopPool.getInstance().detail();
		if (slaves != null && slaves.size() > 0) {
			directTo(slaves, cmd);
		}
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
