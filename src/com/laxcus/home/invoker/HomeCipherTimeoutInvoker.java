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
 * HOME站点密文超时调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public class HomeCipherTimeoutInvoker extends HubCipherTimeoutInvoker {

	/**
	 * 构造HOME站点密文超时调用器，指定命令
	 * @param cmd 密文超时命令
	 */
	public HomeCipherTimeoutInvoker(CipherTimeout cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubCipherTimeoutInvoker#fetchSubSites()
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

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.common.HubCipherTimeoutInvoker#isBottomSite(byte)
//	 */
//	@Override
//	protected boolean isBottomSite(byte family) {
//		return false;
//	}

}
