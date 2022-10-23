/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.site.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;
import com.laxcus.util.set.*;

/**
 * WATCH站点询问分布站点命令调用器。
 * 这个命令由WATCH站点发出，HOME接收并且将站点地址发送给它。
 * 
 * @author scott.liang
 * @version 1.0 3/21/2013
 * @since laxcus 1.0
 */
public class HomeAskSiteInvoker extends HomeInvoker {

	/**
	 * 构造WATCH站点询问分布站点命令调用器，指定命令
	 * @param cmd 收集分布站点命令
	 */
	public HomeAskSiteInvoker(AskSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AskSite getCommand() {
		return (AskSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AskSite cmd = getCommand();
		// 检查来源，只接受WATCH站点命令
		Node endpoint = cmd.getSource().getNode();
		if (!endpoint.isWatch()) {
			return false;
		}

		NodeSet nodes = new NodeSet();
		// 收集HOME MONITOR/LOG/CALL/DATA/WORK/BUILD站点，通知这个WATCH站点
		nodes.pushAll(MonitorOnHomePool.getInstance().list());
		nodes.pushAll(LogOnHomePool.getInstance().list());
		nodes.pushAll(CallOnHomePool.getInstance().list());
		nodes.pushAll(DataOnHomePool.getInstance().list());
		nodes.pushAll(WorkOnHomePool.getInstance().list());
		nodes.pushAll(BuildOnHomePool.getInstance().list());
		// 自己的
		nodes.add(getLocal());
		
		// 应答报告
		AskSiteProduct product = new AskSiteProduct();
		product.addAll(nodes.list());
		// 反馈给WATCH站点
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "send sites:%d to %s", product.size(), endpoint);

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