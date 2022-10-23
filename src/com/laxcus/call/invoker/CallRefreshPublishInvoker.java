/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.command.account.*;
import com.laxcus.command.task.*;
import com.laxcus.call.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;

/**
 * 刷新发布的任务组件调用器。<br><br>
 * 
 * 通知管理池，去ACCOUNT站点下载新的组件，包括分布任务组件、码位计算器。
 * 
 * @author scott.liang
 * @version 1.0 3/13/2018
 * @since laxcus 1.0
 */
public class CallRefreshPublishInvoker extends CallInvoker {

	/**
	 * 构造刷新发布的任务组件调用器，指定命令
	 * @param cmd 刷新发布的任务组件
	 */
	public CallRefreshPublishInvoker(RefreshPublish cmd) {
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
		RefreshPublish cmd = getCommand();
		Siger siger = cmd.getSiger();
		
		// ACCOUNT站点
		Node account = cmd.getRemote();
		// 保存ACCOUNT站点和账号签名的关联
		if (siger != null) {
			AccountOnCommonPool.getInstance().add(siger, account);
			StaffOnCallPool.getInstance().loadTask(siger, cmd.getTaskFamily());
		} else {
			TaskPart part = new TaskPart(siger, cmd.getTaskFamily());
			TakeTaskTag sub = new TakeTaskTag(account, part);
			CallCommandPool.getInstance().admit(sub);
		}

		return useful();
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