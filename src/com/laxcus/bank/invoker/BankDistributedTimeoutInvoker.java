/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;
import java.util.*;

import com.laxcus.command.mix.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.bank.*;
import com.laxcus.bank.pool.*;

/**
 * BANK站点分布处理超时调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 9/15/2019
 * @since laxcus 1.0
 */
public class BankDistributedTimeoutInvoker extends HubDistributedTimeoutInvoker {

	/**
	 * 构造BANK站点分布处理超时调用器，指定命令
	 * @param cmd 分布处理超时命令
	 */
	public BankDistributedTimeoutInvoker(DistributedTimeout cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoker.common.HubDistributedTimeoutInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成子站点
		BankLauncher launcher = (BankLauncher) getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 取出BANK节点下面的全部运行站点
		array.addAll(MonitorOnBankPool.getInstance().detail());
		array.addAll(LogOnBankPool.getInstance().detail());
		array.addAll(AccountOnBankPool.getInstance().detail());
		array.addAll(HashOnBankPool.getInstance().detail());
		array.addAll(GateOnBankPool.getInstance().detail());
		array.addAll(EntranceOnBankPool.getInstance().detail());

		return array;
	}

}