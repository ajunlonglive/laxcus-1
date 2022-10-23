/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;
import com.laxcus.util.set.*;

/**
 * 询问分布站点调用器。
 * 这个命令由WATCH站点发出，TOP接收并且处理它
 * 
 * @author scott.liang
 * @version 1.0 3/21/2013
 * @since laxcus 1.0
 */
public class TopAskSiteInvoker extends TopInvoker {

	/**
	 * 构造询问分布站点调用器，指定命令
	 * @param cmd 询问分布站点命令
	 */
	public TopAskSiteInvoker(AskSite cmd) {
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
		// 收集HOME/TOP MONITOR/LOG/BANK站点地址，通知这个WATCH站点
		nodes.pushAll(MonitorOnTopPool.getInstance().list());
		nodes.pushAll(LogOnTopPool.getInstance().list());
		nodes.pushAll(BankOnTopPool.getInstance().list());
		nodes.pushAll(HomeOnTopPool.getInstance().list());
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
