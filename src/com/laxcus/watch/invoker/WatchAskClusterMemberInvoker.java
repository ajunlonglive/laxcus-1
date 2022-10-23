/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 查询注册用户调用器。<br>
 * WATCH站点向HOME/BANK站点询问集群中注册和在线的注册用户
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class WatchAskClusterMemberInvoker extends WatchInvoker {

	/**
	 * 构造询问分布站点命令调用器，指定命令
	 * @param cmd 询问分布站点命令
	 */
	public WatchAskClusterMemberInvoker(AskClusterMember cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AskClusterMember getCommand() {
		return (AskClusterMember) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 如果没有注册
		if (isLogout()) {
			faultX(FaultTip.SITE_NOT_LOGING);
			return false;
		}

		Node hub = getHub();
		// 发送到TOP/HOME/BANK集群
		AskClusterMember cmd = getCommand();
		boolean success = directTo(hub, cmd);

		// 如果成功则忽略显示，不成功才显示错误信息
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}