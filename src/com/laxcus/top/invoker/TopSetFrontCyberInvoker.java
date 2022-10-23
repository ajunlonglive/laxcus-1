/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.cyber.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.top.*;
import com.laxcus.top.pool.*;

/**
 * TOP站点设置FRONT在线用户调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public class TopSetFrontCyberInvoker extends HubSetFrontCyberInvoker {

	/**
	 * 构造TOP站点设置FRONT在线用户调用器，指定命令
	 * @param cmd 设置FRONT在线用户命令
	 */
	public TopSetFrontCyberInvoker(SetFrontCyber cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoker.common.HubSetFrontCyberInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成
		TopLauncher launcher = (TopLauncher) getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 分配下面的BANK/HOME集群
		array.addAll(HomeOnTopPool.getInstance().detail());
		array.addAll(BankOnTopPool.getInstance().detail());

		return array;
	}

}