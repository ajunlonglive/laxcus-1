/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.gate.pool.*;
import com.laxcus.command.rule.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 撤销事务命令调用器。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2014
 * @since laxcus 1.0
 */
public class GateDetachRuleInvoker extends GateInvoker {

	/**
	 * 构造撤销事务命令调用器，指定命令
	 * @param cmd 撤销事务操作
	 */
	public GateDetachRuleInvoker(DetachRule cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DetachRule getCommand() {
		return (DetachRule) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DetachRule cmd = getCommand();

		// 如果没有定义用户签名，这是一个错误
		if (cmd.getIssuer() == null) {
			// 如果要求反馈
			if (cmd.isReply()) {
				replyFault(Major.FAULTED, Minor.CLIENT_ERROR);
			}
			return false;
		}

		// 回收命令
		boolean success = RuleHouse.getInstance().revoke(cmd);
		// 如果要求返回应答，向源头站点返回处理结果
		if (cmd.isReply()) {
			RuleProduct product = new RuleProduct(cmd.getIssuer(), cmd.getTag(), success);
			replyProduct(product);
		}

		// 打印处理结果
		Logger.debug(this, "launch", success, "revoke %s#%s", cmd.getIssuer(), cmd.getSource());

		// 撤销成功，检查后续处于等待的操作
		if (success) {
			nextTo();
		}

		return useful(success);
	}

	/**
	 * 触发下一个事务规则
	 */
	private void nextTo() {
		DetachRule cmd = getCommand();
		Siger username = cmd.getIssuer();
		// 启动新的处理
		while (true) {
			// 判断有处于等待状态中的事务
			boolean idle = RuleHouse.getInstance().hasIdle(username);
			if (!idle) {
				//				Logger.debug(this, "nextTo", "%s is empty!", username);
				break;
			}

			// 获得一个等待的事务
			AttachRule nextRule = RuleHouse.getInstance().next(username);
			if (nextRule == null) {
				//				Logger.debug(this, "nextTo", "cannot be find %s", username);
				break;
			}
			
			// 向等待的目标站点发送应答
			RuleProduct nextProduct = new RuleProduct(nextRule.getIssuer(), nextRule.getTag(), true);
			// 拿到它的监听地址，发送到指定的地址，通知它发出的事务已经启动
			Cabin listener = nextRule.getSource();
			boolean success = replyProduct(listener, nextProduct);

			// 如果发送不成功，撤销这个事务
			if (!success) {
				DetachRule drop = new DetachRule(nextRule.getTag());
				drop.setIssuer(nextRule.getIssuer());
				drop.addAll(nextRule.list());
				// 删除它
				RuleHouse.getInstance().revoke(drop);
			}

			// 发送到指定站点
			//			Logger.debug(this, "nextTo", "send %s to %s", username, listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}