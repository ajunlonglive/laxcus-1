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
 * 设置应答数据传输模式调用器 <br>
 * 
 * 命令发送到TOP站点，TOP站点要更新集群内的全部站点
 * 
 * @author scott.liang
 * @version 1.0 9/3/2020
 * @since laxcus 1.0
 */
public class TopReplyPacketModeInvoker extends HubReplyPacketModeInvoker {

	/**
	 * 构造设置应答数据传输模式调用器，指定命令
	 * @param cmd 设置应答数据传输模式
	 */
	public TopReplyPacketModeInvoker(ReplyPacketMode cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubReplyPacketModeInvoker#fetchSubSites()
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