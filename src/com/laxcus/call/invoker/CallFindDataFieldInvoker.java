/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.command.field.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 查找DATA站点元数据调用器。<br><br>
 * 
 * 说明：<br>
 * 这个调用器向HOME站点提交命令，不等待HOME站点异步应答。对应的，HOME站点不做异步回应。
 * HOME站点包括“主HOME站点”和“关联HOME站点”。<br><br>
 * 
 * 流程：<br>
 * 1. CALL -> HOME <br>
 * 2. HOME(检查自己配置）-> DATA/WORK/BUILD <br>
 * 3. DATA/WORK/BUILD -> CALL <br><br>
 * 
 * 应答数据是PushField子类。
 * 
 * @author scott.liang
 * @version 1.0 2/21/2013
 * @since laxcus 1.0
 */
public class CallFindDataFieldInvoker extends CallInvoker {

	/**
	 * 构造查找DATA站点元数据调用器，指定命令
	 * @param cmd
	 */
	public CallFindDataFieldInvoker(FindDataField cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Cabin site = this.getListener();
		FindDataField cmd = (FindDataField) super.getCommand();
		// 设置命令的回显地址
		cmd.setSource(site);
		// 设置源地址
		cmd.setNode(site.getNode());

		// 提交命令到“主HOME”站点
		Node hub = super.getLauncher().getHub();
		boolean success = super.submit(hub, cmd);
		
		// 同时提交到关联的HOME站点(RelateHome)

		Logger.debug(this, "launch", success, "send to %s", hub);

		setQuit(true);
		return success;
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
