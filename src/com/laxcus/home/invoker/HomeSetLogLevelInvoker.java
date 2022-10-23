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
import com.laxcus.home.*;
import com.laxcus.home.pool.*;
import com.laxcus.site.*;

/**
 * 设置站点日志等级调用器
 * 
 * @author scott.liang
 * @version 1.0 8/17/2017
 * @since laxcus 1.0
 */
public class HomeSetLogLevelInvoker extends HubSetLogLevelInvoker {

	/**
	 * 构造设置站点日志等级调用器，指定命令
	 * @param cmd 设置站点日志等级
	 */
	public HomeSetLogLevelInvoker(SetLogLevel cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubSetLogLevelInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成子站点
		HomeLauncher launcher = (HomeLauncher) getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 忽略LOG站点，取出HOME节点下面的全部运行站点
		array.addAll(MonitorOnHomePool.getInstance().detail());
		array.addAll(CallOnHomePool.getInstance().detail());
		array.addAll(DataOnHomePool.getInstance().detail());
		array.addAll(BuildOnHomePool.getInstance().detail());
		array.addAll(WorkOnHomePool.getInstance().detail());

		return array;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.common.HubSetLogLevelInvoker#isBottomSite(byte)
//	 */
//	@Override
//	protected boolean isBottomSite(byte family) {
//		return false;
//	}

}