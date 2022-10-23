/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.command.account.*;
import com.laxcus.command.task.*;
import com.laxcus.data.pool.*;
import com.laxcus.pool.archive.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.*;
import com.laxcus.util.naming.*;

/**
 * 刷新发布的任务组件调用器。<br><br>
 * 
 * 通知管理池，去ARCHIVE站点下载新的组件，包括分布任务组件、码位计算器。
 * 
 * @author scott.liang
 * @version 1.0 3/13/2018
 * @since laxcus 1.0
 */
public class DataRefreshPublishInvoker extends DataInvoker {

	/**
	 * 构造刷新发布的任务组件调用器，指定命令
	 * @param cmd 刷新发布的任务组件
	 */
	public DataRefreshPublishInvoker(RefreshPublish cmd) {
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
		
		// 判断是许可
		boolean success = false;
		int taskFamily = cmd.getTaskFamily();
		if (isMaster()) {
			success = PhaseTag.onDataMasterSite(taskFamily);
		} else if (isSlave()) {
			// 保留FROM/RISE, 忽略SCAN节点
			success = PhaseTag.onDataSlaveSite(taskFamily); // (PhaseTag.isFrom(taskFamily) || PhaseTag.isRise(taskFamily));
		}

		if (!success) {
			return useful(false);
		}

		// 保存ACCOUNT站点和账号签名关联
		if (siger != null) {
			AccountOnCommonPool.getInstance().add(siger, account);
			StaffOnDataPool.getInstance().loadTask(siger, cmd.getTaskFamily());
		} else {
			TaskPart part = new TaskPart(siger, cmd.getTaskFamily());
			TakeTaskTag sub = new TakeTaskTag(account, part);
			DataCommandPool.getInstance().admit(sub);
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