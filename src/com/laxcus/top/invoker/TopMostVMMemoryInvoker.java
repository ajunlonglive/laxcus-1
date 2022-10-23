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
 * TOP站点最大虚拟机内存使用率限制调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 1/21/2020
 * @since laxcus 1.0
 */
public class TopMostVMMemoryInvoker extends HubMostVMMemoryInvoker {

	/**
	 * 构造TOP站点最大虚拟机内存使用率限制调用器，指定命令
	 * @param cmd 站点最大虚拟机内存使用率限制命令
	 */
	public TopMostVMMemoryInvoker(MostVMMemory cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubMostVMMemoryInvoker#fetchSubSites()
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

}