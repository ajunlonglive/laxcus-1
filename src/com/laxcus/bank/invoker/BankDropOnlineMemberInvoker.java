/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.bank.pool.*;
import com.laxcus.site.*;

/**
 * 删除在线用户给WATCH站点调用器
 * 
 * @author scott.liang
 * @version 1.0 1/11/2020
 * @since laxcus 1.0
 */
public class BankDropOnlineMemberInvoker extends BankInvoker {

	/**
	 * 构造删除在线用户给WATCH站点，设置命令
	 * @param cmd 删除在线用户给WATCH站点
	 */
	public BankDropOnlineMemberInvoker(DropOnlineMember cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropOnlineMember getCommand() {
		return (DropOnlineMember) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropOnlineMember cmd = getCommand();

		// 转发给TOP节点，全部WATCH站点
		ArrayList<Node> slaves = new ArrayList<Node>();
		slaves.add(getHub()); // TOP节点
		slaves.addAll(WatchOnBankPool.getInstance().detail());
		directTo(slaves, cmd);

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}