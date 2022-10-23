/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;
import com.laxcus.ray.runtime.*;

/**
 * DropSite命令调用器
 * 
 * @author scott.liang
 * @version 1.0 3/21/2013
 * @since laxcus 1.0
 */
public class RayDropSiteInvoker extends RayCastElementInvoker {

	/**
	 * 构造DropSite命令调用器，指定命令
	 * @param cmd DropSite命令
	 */
	public RayDropSiteInvoker(DropSite cmd) {
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
		Node node = cmd.getSite();

//		// 释放站点
//		boolean success = SiteOnWatchPool.getInstance().contains(node);
//		if (success) {
//			success = WatchLauncher.getInstance().dropSite(node);
//		}
		
		// 释放站点
		boolean success = dropSite(node);
		
		// 打印提示
		if (success) {
			messageX(WarningTip.DROP_NODE_X, node);
		}

		// 删除节点运行时记录
		RaySiteRuntimeBasket.getInstance().dropRuntime(node);

		// 不做区别删除
		dropRegisterMember(node);
		dropOnlineMember(node);

		// // 删除节点和所属用户. CALL保存注册用户、在线用户，ACCOUNT删除注册用户，GATE注册在线用户
		// if (node.isCall()) {
		// dropRegisterMember(node);
		// dropOnlineMember(node);
		// } else if (node.isAccount()) {
		// dropRegisterMember(node);
		// } else if (node.isGate()) {
		// dropOnlineMember(node);
		// }

		Logger.debug(this, "launch", success, "node is %s", node);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}