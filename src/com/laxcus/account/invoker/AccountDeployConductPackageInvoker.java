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
 * 部署分布计算调用器
 * 
 * @author scott.liang
 * @version 1.0 3/22/2020
 * @since laxcus 1.0
 */
public class AccountDeployConductPackageInvoker extends AccountDeployCloudPackageInvoker {

	/**
	 * 构造部署分布计算调用器，指定命令
	 * @param cmd 部署分布计算应用包
	 */
	public AccountDeployConductPackageInvoker(DeployConductPackage cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployConductPackage getCommand() {
		return (DeployConductPackage) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDeployCloudPackageInvoker#checkSites()
	 */
	@Override
	protected boolean checkSites() {
		byte[] sites = new byte[] { SiteTag.CALL_SITE, SiteTag.CALL_SITE, SiteTag.DATA_SITE, SiteTag.WORK_SITE };
		byte[] ranks = new byte[] { RankTag.NONE, RankTag.NONE, RankTag.NONE, RankTag.NONE };
		return exists(sites, ranks);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDeployCloudPackageInvoker#deploy(com.laxcus.command.cloud.CloudPackageComponent)
	 */
	@Override
	protected PlayFruit deploy(CloudPackageComponent component) {
		// 分发4个阶段的组件:
		// 1. INIT阶段
		PlayFruit f1 = distribute(PhaseTag.INIT, SiteTag.CALL_SITE, RankTag.NONE, component);
		if(!f1.success) return f1;
		
		// 2. BALANCE阶段
		PlayFruit f2 = distribute(PhaseTag.BALANCE, SiteTag.CALL_SITE, RankTag.NONE, component);
		if(!f2.success) return new PlayFruit(false, f1.count + f2.count);
		
		// 3. FROM阶段
		PlayFruit f3 = distribute(PhaseTag.FROM, SiteTag.DATA_SITE, RankTag.NONE, component);
		if(!f3.success) return new PlayFruit(false, f1.count + f2.count + f3.count);
		
		// 4. TO阶段
		PlayFruit f4 = distribute(PhaseTag.TO, SiteTag.WORK_SITE, RankTag.NONE, component);
		if(!f4.success) return new PlayFruit(false, f1.count + f2.count + f3.count + f4.count);
		
		// 统计值
		int count = f1.count + f2.count + f3.count + f4.count;

		Logger.debug(this, "distribute", "count is %d", count);

		return new PlayFruit(true, count);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.account.invoker.AccountDeployCloudPackageInvoker#refreshPublish()
	 */
	@Override
	protected boolean refreshPublish() {
		int[] families = new int[] { PhaseTag.INIT, PhaseTag.BALANCE,
				PhaseTag.FROM, PhaseTag.TO };

		return refreshPublish(families);
	}

}