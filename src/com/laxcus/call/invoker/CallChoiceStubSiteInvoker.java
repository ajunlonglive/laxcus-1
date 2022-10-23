/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.call.pool.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.util.set.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * ChoiceStubSite命令调用器 <br>
 * 
 * 命令从WORK站点发出，CALL站点接收，从内存中给每个数据块编号选择一个关联的站点地址（不区分主从，每个编号选择一个）。
 * 
 * @author scott.liang
 * @version 06/20/2013
 * @since laxcus 1.0
 */
public class CallChoiceStubSiteInvoker extends CallInvoker {

	/**
	 * 构造ChoiceStubSite命令调用器
	 * @param cmd - ChoiceStubSite命令
	 */
	public CallChoiceStubSiteInvoker(ChoiceStubSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ChoiceStubSite getCommand() {
		return (ChoiceStubSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ChoiceStubSite cmd = getCommand();

		FindStubSiteProduct product = new FindStubSiteProduct();

		for (long stub : cmd.list()) {
			NodeSet set = DataOnCallPool.getInstance().findStubSites(stub);
			if (set == null) {
				continue;
			}
			Node hub = set.next();
			if (hub == null) {
				continue;
			}
			// 保存参数
			product.add(hub, stub);
		}

		boolean success = replyProduct(product);
		
		Logger.debug(this, "launch", success, "StubEntry size:%d, StubEntry size %d", cmd.size(), product.size());

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
