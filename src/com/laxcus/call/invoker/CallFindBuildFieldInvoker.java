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
 * 查找BUILD站点元数据调用器。<br><br>
 * 
 * 目标是HOME站点，发送后退出。异步应答是HOME站点发出PushBuildField命令。
 * 
 * @author scott.liang
 * @version 1.0 2/21/2013
 * @since laxcus 1.0
 */
public class CallFindBuildFieldInvoker extends CallInvoker {

	/**
	 * 构造查找BUILD站点元数据调用器，指定命令
	 * @param cmd
	 */
	public CallFindBuildFieldInvoker(FindBuildField cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Cabin site = this.getListener();
		FindBuildField cmd = (FindBuildField) super.getCommand();
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
