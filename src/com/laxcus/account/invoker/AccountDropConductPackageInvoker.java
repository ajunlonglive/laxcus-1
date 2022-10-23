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
 * 删除分布计算应用调用器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2020
 * @since laxcus 1.0
 */
public class AccountDropConductPackageInvoker extends AccountDropCloudPackageInvoker {

	/**
	 * 构造删除分布计算应用调用器，指定命令
	 * @param cmd 删除分布计算应用应用包
	 */
	public AccountDropConductPackageInvoker(DropConductPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropConductPackage getCommand() {
		return (DropConductPackage) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDropCloudPackageInvoker#drop()
	 */
	@Override
	protected int drop() {
		Logger.debug(this, "drop,", "todo");

		// 删除4个阶段的组件:
		// 1. INIT阶段
		int ret = drop(PhaseTag.INIT, SiteTag.CALL_SITE, RankTag.NONE);
		if(ret < 1) return -1;
		int count = ret;
		// 2. BALANCE阶段
		ret = drop(PhaseTag.BALANCE, SiteTag.CALL_SITE, RankTag.NONE);
		if(ret < 1) return -1;
		count += ret;
		// 3. FROM阶段
		ret = drop(PhaseTag.FROM, SiteTag.DATA_SITE, RankTag.NONE);
		if(ret < 1) return -1;
		count += ret;
		// 4. TO阶段
		ret = drop(PhaseTag.TO, SiteTag.WORK_SITE, RankTag.NONE);
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
		DropConductPackage cmd = getCommand();
		TaskPart part = new TaskPart(cmd.getIssuer(), PhaseTag.INIT);
		return WareOnAccountPool.getInstance().delete(part, cmd.getWare());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDropCloudPackageInvoker#refreshPublish()
	 */
	@Override
	protected boolean refreshPublish() {
		int[] families = new int[] { PhaseTag.INIT, PhaseTag.BALANCE,
				PhaseTag.FROM, PhaseTag.TO };

		return refreshPublish(families);
	}

}