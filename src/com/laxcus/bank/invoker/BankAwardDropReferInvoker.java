/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.bank.pool.*;
import com.laxcus.command.refer.*;
import com.laxcus.site.*;

/**
 * 删除用户资源引用调用器。
 * 
 * BANK原样转发给TOP站点，TOP站点分发给下属的HOME站点。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class BankAwardDropReferInvoker extends BankInvoker {

	/**
	 * 构造删除用户资源引用调用器，指定命令
	 * @param cmd 删除用户资源引用
	 */
	public BankAwardDropReferInvoker(AwardDropRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardDropRefer getCommand() {
		return (AwardDropRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 投递给HASH/GATE站点
		directToSlaves();
		// 转发命令给TOP站点
		return transmit();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 接收TOP反馈
		return reflect();
	}

	/**
	 * 向全部HASH/GATE站点，投递删除操作，此处是单向命令，不用反馈 
	 */
	private void directToSlaves() {
		ArrayList<Node> array = new ArrayList<Node>();
		array.addAll(HashOnBankPool.getInstance().detail());
		array.addAll(GateOnBankPool.getInstance().detail());
		directTo(array, getCommand());
	}
}
