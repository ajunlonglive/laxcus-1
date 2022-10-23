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
 * 推送被销毁站点调用器
 * 
 * @author scott.liang
 * @version 1.0 12/11/2011
 * @since laxcus 1.0
 */
public class TopDestroySiteInvoker extends TopInvoker {

	/**
	 * 构造推送被销毁站点调用器，指定命令
	 * @param cmd 推送被销毁站点
	 */
	public TopDestroySiteInvoker(DestroySite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DestroySite getCommand() {
		return (DestroySite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DestroySite cmd = getCommand();
		// 判断是监视站点
		boolean success = isMonitor();
		// 保存这个站点
		if (success) {
			Node node = cmd.getSite();
			success = getLauncher().drop(node);
		}
		
		// 推送给WATCH节点，因为TOP.WATCH节点保存有HOME/BANK子域集群的节点用户签名
		pushToWatch(cmd);

		Logger.debug(this, "launch", success, "destroy %s", cmd.getSite());

		return useful(success);
	}
	
	/**
	 * 推送给TOP.WATCH节点
	 * @param cmd
	 */
	private void pushToWatch(DestroySite cmd) {
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
