/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.command.missing.*;
import com.laxcus.bank.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 许可证超期调用器。<BR>
 * 找到WATCH站点，发送给它！
 * 
 * @author scott.liang
 * @version 1.0 7/18/2020
 * @since laxcus 1.0
 */
public class BankLicenceTimeoutInvoker extends BankInvoker {

	/**
	 * 构造许可证超期调用器，指定命令
	 * @param cmd 许可证超期
	 */
	public BankLicenceTimeoutInvoker(LicenceTimeout cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public LicenceTimeout getCommand() {
		return (LicenceTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.add(getHub()); // TOP节点

		// 找到全部WATCH节点
		List<Node> slaves = WatchOnBankPool.getInstance().detail();
		if (!slaves.isEmpty()) {
			nodes.addAll(slaves);
		}
		// 本地地址
		LicenceTimeout cmd = getCommand();

		// 投递到指定的WATCH节点，不需要反馈
		int count = directTo(nodes, cmd);

		// 统计
		boolean success = (count > 0);

		Logger.debug(this, "launch", success, "direct count %d", count);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
