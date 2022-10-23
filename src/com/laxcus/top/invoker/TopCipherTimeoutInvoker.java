/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.top.*;
import com.laxcus.top.pool.*;

/**
 * TOP站点密文超时调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public class TopCipherTimeoutInvoker extends HubCipherTimeoutInvoker {

	/**
	 * 构造TOP站点密文超时调用器，指定命令
	 * @param cmd 密文超时命令
	 */
	public TopCipherTimeoutInvoker(CipherTimeout cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubCipherTimeoutInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成
		TopLauncher launcher = (TopLauncher) getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 取出TOP节点下面的全部运行站点
		array.addAll(MonitorOnTopPool.getInstance().detail());
		array.addAll(HomeOnTopPool.getInstance().detail());
		array.addAll(LogOnTopPool.getInstance().detail());
		array.addAll(BankOnTopPool.getInstance().detail());
		
		
		return array;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.common.HubMaxEchoBufferInvoker#isBottomSite(byte)
//	 */
//	@Override
//	protected boolean isBottomSite(byte family) {
//		switch (family) {
//		case SiteTag.DATA_SITE:
//		case SiteTag.BUILD_SITE:
//		case SiteTag.WORK_SITE:
//		case SiteTag.CALL_SITE:
//		case SiteTag.ACCOUNT_SITE:
//		case SiteTag.HASH_SITE:
//		case SiteTag.GATE_SITE:
//		case SiteTag.ENTRANCE_SITE:
//		case SiteTag.LOG_SITE:
//			return true;
//		default:
//			return false;
//		}
//	}

}
