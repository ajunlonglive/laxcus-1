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
 * 删除数据构建应用调用器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2020
 * @since laxcus 1.0
 */
public class AccountDropEstablishPackageInvoker extends AccountDropCloudPackageInvoker {

	/**
	 * 构造删除数据构建应用调用器，指定命令
	 * @param cmd 删除数据构建应用应用包
	 */
	public AccountDropEstablishPackageInvoker(DropEstablishPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropEstablishPackage getCommand() {
		return (DropEstablishPackage) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDropCloudPackageInvoker#drop()
	 */
	@Override
	protected int drop() {
		Logger.debug(this, "drop,", "todo");

		// 删除3个阶段的组件:
		// 1. ISSUE阶段
		int ret = drop(PhaseTag.ISSUE, SiteTag.CALL_SITE, RankTag.NONE);
		if(ret < 1) return -1;
		int count = ret;
		// 2. ASSIGN阶段
		ret = drop(PhaseTag.ASSIGN, SiteTag.CALL_SITE, RankTag.NONE);
		if(ret < 1) return -1;
		count += ret;
		// 3. SCAN阶段，只指向DATA主节点！
		ret = drop(PhaseTag.SCAN, SiteTag.DATA_SITE, RankTag.MASTER);
		if(ret < 1) return -1;
		count += ret;
		// 4. SIFT阶段，指向BUILD节点
		ret = drop(PhaseTag.SIFT, SiteTag.BUILD_SITE, RankTag.NONE);
		if(ret < 1) return -1;
		count += ret;
		// 5. RISE阶段，指向DATA节点（包括主节点、从节点）
		ret = drop(PhaseTag.RISE, SiteTag.DATA_SITE, RankTag.NONE);
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
		DropEstablishPackage cmd = getCommand();
		TaskPart part = new TaskPart(cmd.getIssuer(), PhaseTag.ISSUE);
		return WareOnAccountPool.getInstance().delete(part, cmd.getWare());
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDropCloudPackageInvoker#refreshPublish()
	 */
	@Override
	protected boolean refreshPublish() {
		int[] families = new int[] { PhaseTag.ISSUE, PhaseTag.ASSIGN,
				PhaseTag.SCAN, PhaseTag.SIFT, PhaseTag.RISE };

		return refreshPublish(families);
	}

}