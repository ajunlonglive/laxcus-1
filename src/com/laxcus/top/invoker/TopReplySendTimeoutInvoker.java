/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.reload.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.top.*;
import com.laxcus.top.pool.*;

/**
 * 设置发送异步数据超时调用器 <br>
 * 
 * 命令发送到TOP站点，TOP站点要更新集群内的全部站点
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class TopReplySendTimeoutInvoker extends HubReplySendTimeoutInvoker {

	/**
	 * 构造设置发送异步数据超时调用器，指定命令
	 * @param cmd 设置发送异步数据超时
	 */
	public TopReplySendTimeoutInvoker(ReplySendTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubReplySendTimeoutInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成
		TopLauncher launcher = (TopLauncher) super.getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 取出TOP节点下面的全部站点地址
		array.addAll(MonitorOnTopPool.getInstance().detail());
		array.addAll(HomeOnTopPool.getInstance().detail());
		array.addAll(LogOnTopPool.getInstance().detail());
		array.addAll(BankOnTopPool.getInstance().detail());
		
		return array;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.common.HubReplySendTimeoutInvoker#isBottomSite(byte)
//	 */
//	@Override
//	protected boolean isBottomSite(byte family) {
//		switch (family) {
//		case SiteTag.LOG_SITE:
//			// HOME集群
//		case SiteTag.DATA_SITE:
//		case SiteTag.BUILD_SITE:
//		case SiteTag.WORK_SITE:
//		case SiteTag.CALL_SITE:
//			// BANK集群
//		case SiteTag.ACCOUNT_SITE:
//		case SiteTag.HASH_SITE:
//		case SiteTag.GATE_SITE:
//		case SiteTag.ENTRANCE_SITE:
//			return true;
//		default:
//			return false;
//		}
//	}

}