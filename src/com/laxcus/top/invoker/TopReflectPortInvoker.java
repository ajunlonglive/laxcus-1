/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.top.*;
import com.laxcus.top.pool.*;

/**
 * 设置映射端口调用器 <br>
 * 
 * 命令接收到TOP站点，TOP站点要更新集群内的全部站点
 * 
 * @author scott.liang
 * @version 1.0 9/11/2020
 * @since laxcus 1.0
 */
public class TopReflectPortInvoker extends HubReflectPortInvoker {

	/**
	 * 构造设置映射端口调用器，指定命令
	 * @param cmd 设置映射端口
	 */
	public TopReflectPortInvoker(ReflectPort cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubReflectPortInvoker#fetchSubSites()
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