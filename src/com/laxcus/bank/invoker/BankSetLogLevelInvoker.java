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
import com.laxcus.bank.*;
import com.laxcus.bank.pool.*;
import com.laxcus.site.*;

/**
 * 设置站点日志等级调用器
 * 
 * @author scott.liang
 * @version 1.0 6/26/2018
 * @since laxcus 1.0
 */
public class BankSetLogLevelInvoker extends HubSetLogLevelInvoker {

	/**
	 * 构造设置站点日志等级调用器，指定命令
	 * @param cmd 设置站点日志等级
	 */
	public BankSetLogLevelInvoker(SetLogLevel cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.common.HubSetLogLevelInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成子站点
		BankLauncher launcher = (BankLauncher) getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 忽略LOG站点，取出BANK节点下面的全部运行站点
		array.addAll(MonitorOnBankPool.getInstance().detail());
		array.addAll(AccountOnBankPool.getInstance().detail());
		array.addAll(HashOnBankPool.getInstance().detail());
		array.addAll(GateOnBankPool.getInstance().detail());
		array.addAll(EntranceOnBankPool.getInstance().detail());

		return array;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see com.laxcus.echo.invoke.common.HubSetLogLevelInvoker#isBottomSite(byte)
//	 */
//	@Override
//	protected boolean isBottomSite(byte family) {
//		return false;
//	}

}