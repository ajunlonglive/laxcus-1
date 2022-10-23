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
import com.laxcus.command.site.gate.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 检查GATE站点的注册用户和站点编号的一致性调用器
 * 
 * @author scott.liang
 * @version 1.0 7/20/2019
 * @since laxcus 1.0
 */
public class BankCheckShadowConsistencyInvoker extends BankInvoker {

	/**
	 * 构造检查GATE站点的注册用户和站点编号的一致性调用器，指定命令
	 * @param cmd 检查GATE站点的注册用户和站点编号的一致性
	 */
	public BankCheckShadowConsistencyInvoker(CheckShadowConsistency cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckShadowConsistency getCommand() {
		return (CheckShadowConsistency) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ArrayList<Node> slaves = new ArrayList<Node>();

		// 取出全部GATE站点
		List<Node> nodes = GateOnBankPool.getInstance().detail();
		CheckShadowConsistency cmd = getCommand();
		if (cmd.isAll()) {
			slaves.addAll(nodes);
		} else {
			for (Node e : cmd.list()) {
				if (nodes.contains(e)) {
					slaves.add(e);
				}
			}
		}

		// 空，是错误
		if (slaves.isEmpty()) {
			replyFault(Major.FAULTED, Minor.SITE_NOTFOUND);
			return useful(false);
		}
		
		// 发送给GATE站点的命令，确定全部GATE站点数目
		CheckShadowConsistency sub = new CheckShadowConsistency();
		sub.setCount(nodes.size());

		// 发送命令
		boolean success = launchTo(slaves, sub);
		if (!success) {
			replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		CheckShadowConsistencyProduct product = new CheckShadowConsistencyProduct();
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessCompleted(index)) {
					CheckShadowConsistencyProduct e = getObject(CheckShadowConsistencyProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 反馈结果
		replyProduct(product);

		return useful();
	}

}
