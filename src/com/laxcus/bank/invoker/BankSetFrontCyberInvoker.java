/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.command.cyber.*;
import com.laxcus.echo.invoker.common.*;
import com.laxcus.site.*;
import com.laxcus.bank.*;
import com.laxcus.bank.pool.*;

/**
 * BANK站点设置FRONT在线用户虚拟空间调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public class BankSetFrontCyberInvoker extends HubSetFrontCyberInvoker {

	/**
	 * 构造BANK站点设置FRONT在线用户虚拟空间调用器，指定命令
	 * @param cmd 设置FRONT在线用户虚拟空间命令
	 */
	public BankSetFrontCyberInvoker(SetFrontCyber cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoker.common.HubSetFrontCyberInvoker#fetchSubSites()
	 */
	@Override
	protected List<Node> fetchSubSites() {
		ArrayList<Node> array = new ArrayList<Node>();

		// 如果是监视站点，不生成子站点
		BankLauncher launcher = (BankLauncher) getLauncher();
		if (launcher.isMonitor()) {
			return array;
		}

		// 取出BANK节点下面的GATE
		array.addAll(GateOnBankPool.getInstance().detail());

		return array;
	}

}