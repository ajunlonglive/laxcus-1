/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.command.reload.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.bank.*;
import com.laxcus.bank.pool.*;
import com.laxcus.site.*;

/**
 * 设置发送异步数据超时调用器 <br>
 * 
 * @author scott.liang
 * @version 1.0 9/1/2020
 * @since laxcus 1.0
 */
public class BankReplySendTimeoutInvoker extends HubReplySendTimeoutInvoker {

	/**
	 * 构造设置发送异步数据超时调用器，指定命令
	 * @param cmd 设置发送异步数据超时
	 */
	public BankReplySendTimeoutInvoker(ReplySendTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubReplySendTimeoutInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成子站点
		BankLauncher launcher =  (BankLauncher) getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 取出BANK节点下面的全部站点地址
		array.addAll(MonitorOnBankPool.getInstance().detail());
		array.addAll(LogOnBankPool.getInstance().detail());
		array.addAll(AccountOnBankPool.getInstance().detail());
		array.addAll(HashOnBankPool.getInstance().detail());
		array.addAll(GateOnBankPool.getInstance().detail());
		array.addAll(EntranceOnBankPool.getInstance().detail());

		return array;
	}

}