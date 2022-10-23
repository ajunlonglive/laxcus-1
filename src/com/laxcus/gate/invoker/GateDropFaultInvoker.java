/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.gate.pool.*;
import com.laxcus.command.limit.*;
import com.laxcus.echo.*;
import com.laxcus.law.limit.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 撤销故障锁定命令调用器 <br>
 * 命令从FRONT站点，GATE站点接收后，从内存中删除锁定单元单元，解除对资源的锁定。
 * 
 * @author scott.liang
 * @version 1.0 3/24/2017
 * @since laxcus 1.0
 */
public class GateDropFaultInvoker extends GateInvoker {

	/**
	 * 构造撤销故障锁定命令调用器，指定命令
	 * @param cmd 撤销故障锁定命令
	 */
	public GateDropFaultInvoker(DropFault cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropFault getCommand() {
		return (DropFault) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropFault cmd = getCommand();
		Siger issuer = cmd.getIssuer();

		boolean success = StaffOnGatePool.getInstance().contains(issuer);
		if (!success) {
			if (cmd.isReply()) replyFault(Major.FAULTED, Minor.REFUSE);
			return useful(false);
		}

		List<LimitItem> limits = StaffOnGatePool.getInstance().dress(issuer, cmd.list());

		// 撤销故障锁定命令
		DropFaultProduct product = new DropFaultProduct();
		if (limits.size() > 0) {
			List<LimitItem> results = LimitHouse.getInstance().revoke(issuer, limits);
			product.addAll(results);
		}

		// 反馈给请求端
		if (cmd.isReply()) {
			success = replyProduct(product);
		}

		Logger.debug(this, "launch", success, "limit item:%d", product.size());

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
