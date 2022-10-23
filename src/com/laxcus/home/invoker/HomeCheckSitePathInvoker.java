/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.home.*;
import com.laxcus.home.pool.*;

/**
 * HOME打印站点检测目录调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/19/2019
 * @since laxcus 1.0
 */
public class HomeCheckSitePathInvoker extends HubCheckSitePathInvoker {

	/**
	 * 构造HOME打印站点检测目录调用器，指定命令
	 * @param cmd 打印站点检测目录命令
	 */
	public HomeCheckSitePathInvoker(CheckSitePath cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoker.common.HubCheckSitePathInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成子站点
		HomeLauncher launcher = (HomeLauncher) getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 取出HOME节点下面的全部运行站点
		array.addAll(MonitorOnHomePool.getInstance().detail());
		array.addAll(CallOnHomePool.getInstance().detail());
		array.addAll(DataOnHomePool.getInstance().detail());
		array.addAll(BuildOnHomePool.getInstance().detail());
		array.addAll(WorkOnHomePool.getInstance().detail());
		array.addAll(LogOnHomePool.getInstance().detail());

		return array;
	}

}
