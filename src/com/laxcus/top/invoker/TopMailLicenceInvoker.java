/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.licence.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.top.*;
import com.laxcus.top.pool.*;

/**
 * 发布许可证调用器 <br>
 * 
 * 命令发送到TOP站点，TOP站点要更新集群内的全部站点
 * 
 * @author scott.liang
 * @version 1.0 7/21/2020
 * @since laxcus 1.0
 */
public class TopMailLicenceInvoker extends HubMailLicenceInvoker {

	/**
	 * 构造发布许可证调用器，指定命令
	 * @param cmd 发布许可证
	 */
	public TopMailLicenceInvoker(MailLicence cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubMailSecurityManagerInvoker#fetchSubSites()
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

}