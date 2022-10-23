/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.account.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.*;
import com.laxcus.util.set.*;
import com.laxcus.site.*;

/**
 * 刷新发布的任务组件调用器
 * 
 * @author scott.liang
 * @version 1.0 3/13/2018
 * @since laxcus 1.0
 */
public class TopRefreshPublishInvoker extends TopInvoker {

	/**
	 * 构造刷新发布的任务组件调用器，指定命令
	 * @param cmd 刷新发布的任务组件
	 */
	public TopRefreshPublishInvoker(RefreshPublish cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshPublish getCommand() {
		return (RefreshPublish) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果是监视器节点，忽略这个命令
		if (isMonitor()) {
			return useful(false);
		}
		
		RefreshPublish cmd = getCommand();
		Siger siger = cmd.getSiger();

		ArrayList<Node> slaves = new ArrayList<Node>();
		
		// 找到与签名关联的HOME站点
		if (siger != null) {
			NodeSet set = HomeOnTopPool.getInstance().findSites(siger);
			if (set != null) {
				slaves.addAll(set.show());
			}
		} else {
			// 全部HOME地址
			slaves.addAll(HomeOnTopPool.getInstance().detail());
		}
		// 投递给HOME站点
		int count = directTo(slaves, cmd);
		boolean success = (count > 0);
		// 退出
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
