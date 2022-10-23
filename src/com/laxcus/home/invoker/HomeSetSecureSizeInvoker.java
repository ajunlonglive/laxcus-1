/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.secure.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.home.*;
import com.laxcus.home.pool.*;
import com.laxcus.site.*;

/**
 * 设置对称密钥长度调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 2/27/2021
 * @since laxcus 1.0
 */
public class HomeSetSecureSizeInvoker extends HubSetSecureSizeInvoker {

	/**
	 * 构造设置对称密钥长度调用器，指定命令
	 * @param cmd 设置对称密钥长度
	 */
	public HomeSetSecureSizeInvoker(SetSecureSize cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubSetSecureSizeInvoker#fetchSubSites()
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

}