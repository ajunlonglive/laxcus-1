/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.reload.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.home.*;
import com.laxcus.home.pool.*;
import com.laxcus.site.*;

/**
 * 重新设置节点的安全策略命令调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 6/17/2018
 * @since laxcus 1.0
 */
public class HomeReloadSecurityPolicyInvoker extends HubReloadSecurityPolicyInvoker {

	/**
	 * 构造重新设置节点的安全策略命令调用器，指定命令
	 * @param cmd 重新设置节点的安全策略命令
	 */
	public HomeReloadSecurityPolicyInvoker(ReloadSecurityPolicy cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubReloadSecurityManagerInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成子站点
		HomeLauncher launcher =  (HomeLauncher) getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 取出HOME节点下面的全部站点地址
		array.addAll(MonitorOnHomePool.getInstance().detail());
		array.addAll(LogOnHomePool.getInstance().detail());
		array.addAll(CallOnHomePool.getInstance().detail());
		array.addAll(DataOnHomePool.getInstance().detail());
		array.addAll(BuildOnHomePool.getInstance().detail());
		array.addAll(WorkOnHomePool.getInstance().detail());

		return array;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.common.HubReloadSecurityManagerInvoker#isBottomSite(byte)
//	 */
//	@Override
//	protected boolean isBottomSite(byte family) {
//		return false;
//	}

}