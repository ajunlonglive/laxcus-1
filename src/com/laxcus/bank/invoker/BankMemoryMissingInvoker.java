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
 * 内存空间不足调用器。<BR>
 * 找到WATCH站点，发送给它！
 * 
 * @author scott.liang
 * @version 1.0 6/14/2019
 * @since laxcus 1.0
 */
public class BankMemoryMissingInvoker extends BankInvoker {

	/**
	 * 构造内存空间不足调用器，指定命令
	 * @param cmd 内存空间不足
	 */
	public BankMemoryMissingInvoker(MemoryMissing cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MemoryMissing getCommand() {
		return (MemoryMissing) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		List<Node> slaves = WatchOnBankPool.getInstance().detail();
		if (slaves.isEmpty()) {
			Logger.warning(this, "launch", "not found Watch Site!");
			return useful(false);
		}
		// 本地地址
		MemoryMissing cmd = getCommand();

		// 如果没有定义来源地址，是Bank节点自己发的。
		MemoryMissing sub = (cmd.getSite() == null ? 
				new MemoryMissing(getLocal()) : cmd.duplicate());

		// 投递到指定的WATCH节点，不需要反馈
		int count = directTo(slaves, sub);

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
