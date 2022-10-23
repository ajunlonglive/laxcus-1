/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.account.pool.*;
import com.laxcus.command.cloud.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.task.archive.*;
import com.laxcus.util.naming.*;

/**
 * 删除快速计算应用调用器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2020
 * @since laxcus 1.0
 */
public class AccountDropContactPackageInvoker extends AccountDropCloudPackageInvoker {

	/**
	 * 构造删除快速计算应用调用器，指定命令
	 * @param cmd 删除快速计算应用应用包
	 */
	public AccountDropContactPackageInvoker(DropContactPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropContactPackage getCommand() {
		return (DropContactPackage) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDropCloudPackageInvoker#drop()
	 */
	@Override
	protected int drop() {
		Logger.debug(this, "drop,", "todo");

		// 删除3个阶段的组件:

		// 1. FORK阶段
		int ret = drop(PhaseTag.FORK, SiteTag.CALL_SITE, RankTag.NONE);
		if(ret < 1) return -1;
		int count = ret;
		// 2. MERGE阶段
		ret = drop(PhaseTag.MERGE, SiteTag.CALL_SITE, RankTag.NONE);
		if(ret < 1) return -1;
		count += ret;
		// 3. DISTANT阶段
		ret = drop(PhaseTag.DISTANT, SiteTag.WORK_SITE, RankTag.NONE);
		if(ret < 1) return -1;
		count += ret;

		Logger.debug(this, "drop", "file count: %d", count);

		return count;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDropCloudPackageInvoker#delete()
	 */
	@Override
	protected boolean delete() {
		DropContactPackage cmd = getCommand();
		TaskPart part = new TaskPart(cmd.getIssuer(), PhaseTag.FORK);
		return WareOnAccountPool.getInstance().delete(part, cmd.getWare());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDropCloudPackageInvoker#refreshPublish()
	 */
	@Override
	protected boolean refreshPublish() {
		int[] families = new int[] { PhaseTag.FORK, PhaseTag.MERGE,
				PhaseTag.DISTANT };

		return refreshPublish(families);
	}

}