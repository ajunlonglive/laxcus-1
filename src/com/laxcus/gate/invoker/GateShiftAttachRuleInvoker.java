/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.rule.*;
import com.laxcus.echo.*;
import com.laxcus.gate.pool.*;
import com.laxcus.log.client.*;

/**
 * 绑定规则转发命令调用器。<br>
 * 
 * 通知关联的FRONT站点，事务规则已经绑定，可以启动后续的分布处理工作。
 * 
 * @author scott.liang
 * @version 1.0 4/29/2018
 * @since laxcus 1.0
 */
public class GateShiftAttachRuleInvoker extends GateInvoker {

	/**
	 * 构造绑定规则转发命令调用器，指定命令
	 * @param cmd 绑定规则转发命令
	 */
	public GateShiftAttachRuleInvoker(ShiftAttachRule cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftAttachRule getCommand() {
		return (ShiftAttachRule) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftAttachRule shift = getCommand();
		AttachRule cmd = shift.getCommand();
		AttachRuleHook hook = shift.getHook();

		// 向等待的目标站点发送应答
		RuleProduct product = new RuleProduct(cmd.getIssuer(), cmd.getTag(), true);
		// 拿到FRONT的监听地址，发送到指定的地址，通知它发出的事务已经启动
		Cabin listener = cmd.getSource();
		boolean success = replyProduct(listener, product);
		
		Logger.debug(this, "launch", success, "reply %s # %s", cmd.getTag(), listener);

		// 如果发送不成功，撤销这个事务
		if (!success) {
			DetachRule detach = new DetachRule(cmd.getTag());
			detach.setIssuer(cmd.getIssuer());
			detach.addAll(cmd.list());
			// 删除它
			RuleHouse.getInstance().revoke(detach);
		}

		// 通知钩子
		hook.setResult(success);
		hook.done();

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

}