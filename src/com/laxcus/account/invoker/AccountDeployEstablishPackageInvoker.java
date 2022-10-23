/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.command.cloud.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.naming.*;

/**
 * 部署分布数据构建应用调用器
 * 
 * @author scott.liang
 * @version 1.0 3/23/2020
 * @since laxcus 1.0
 */
public class AccountDeployEstablishPackageInvoker extends AccountDeployCloudPackageInvoker {

	/**
	 * 构造部署分布数据构建应用调用器，指定命令
	 * @param cmd 部署分布数据构建应用应用包
	 */
	public AccountDeployEstablishPackageInvoker(DeployEstablishPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployEstablishPackage getCommand() {
		return (DeployEstablishPackage)super.getCommand();
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.account.invoker.AccountDeployCloudPackageInvoker#checkPermission()
//	 */
//	@Override
//	protected boolean checkPermission() {
//		Account account = readAccount();
//		boolean success = (account != null);
//		// 判断允许发布任务
//		if (success) {
//			success = account.canPublishTask();
//		}
//		// 判断允许发布动态链接库
//		if (success) {
//			DeployEstablishPackage cmd = getCommand();
//			if (cmd.hasLibrary()) {
//				success = account.canPublishTaskLibrary();
//			}
//		}
//		return success;
//	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDeployCloudPackageInvoker#checkSites()
	 */
	@Override
	protected boolean checkSites() {
		byte[] sites = new byte[] { SiteTag.CALL_SITE, SiteTag.CALL_SITE,
				SiteTag.DATA_SITE, SiteTag.BUILD_SITE, SiteTag.DATA_SITE };
		
		byte[] ranks = new byte[] { RankTag.NONE, RankTag.NONE, RankTag.MASTER,
				RankTag.NONE, RankTag.NONE };
		return exists(sites, ranks);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDeployCloudPackageInvoker#deploy(com.laxcus.command.cloud.CloudPackageComponent)
	 */
	@Override
	protected PlayFruit deploy(CloudPackageComponent component) {
		// 分发5个阶段的组件
		// 1. ISSUE阶段
		PlayFruit f1 = distribute(PhaseTag.ISSUE, SiteTag.CALL_SITE, RankTag.NONE, component);
		if(!f1.success) return f1;

		// 2. ASSIGN阶段
		PlayFruit f2 = distribute(PhaseTag.ASSIGN, SiteTag.CALL_SITE, RankTag.NONE, component);
		if(!f2.success) return new PlayFruit(false, f1.count + f2.count);

		// 3. SCAN阶段，只指向DATA主节点！
		PlayFruit f3 = distribute(PhaseTag.SCAN, SiteTag.DATA_SITE, RankTag.MASTER, component);
		if(!f3.success) return new PlayFruit(false, f1.count + f2.count + f3.count);

		// 4. SIFT阶段，指向BUILD节点
		PlayFruit f4 = distribute(PhaseTag.SIFT, SiteTag.BUILD_SITE, RankTag.NONE, component);
		if(!f4.success) return new PlayFruit(false, f1.count + f2.count + f3.count + f4.count);

		// 5. RISE阶段，指向DATA节点（包括主节点、从节点）
		PlayFruit f5 = distribute(PhaseTag.RISE, SiteTag.DATA_SITE, RankTag.NONE, component);
		if(!f5.success) return new PlayFruit(false, f1.count + f2.count + f3.count + f4.count + f5.count);
		
		int count = f1.count + f2.count + f3.count + f4.count + f5.count;

		Logger.debug(this, "distribute", "count is %d", count);
		
		return new PlayFruit(true, count);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDeployCloudPackageInvoker#refreshPublish()
	 */
	@Override
	protected boolean refreshPublish() {
		int[] families = new int[] { PhaseTag.ISSUE, PhaseTag.ASSIGN,
				PhaseTag.SCAN, PhaseTag.SIFT, PhaseTag.RISE };

		return refreshPublish(families);
	}

}