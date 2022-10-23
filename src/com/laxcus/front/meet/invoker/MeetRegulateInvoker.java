/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;
import com.laxcus.site.Node;

/**
 * 数据优化异步调用器。<br>
 * 
 * 数据优化只针对一个数据表。
 * 
 * @author scott.liang
 * @version 1.1 9/2/2012
 * @since laxcus 1.0
 */
public class MeetRegulateInvoker extends MeetRuleInvoker {

	/** 操作步骤，从1开始 **/
	private int step = 1;

	/**
	 * 构造数据优化命令的异步调用器，指定数据优化命令
	 * @param cmd 数据优化命令
	 */
	public MeetRegulateInvoker(Regulate cmd) {
		super(cmd);
		init();
	}

	/**
	 * 设置规则
	 */
	private void init() {
		Regulate cmd = getCommand();
		Space space = cmd.getSpace();
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(space);
		addRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Regulate getCommand() {
		return (Regulate) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		boolean success = false;
		switch (step) {
		case 1:
			success = send();
			break;
		case 2:
			success = receive();
			break;
		}
		// 自增1
		step++;
		// 不成功，或者大于2是退出
		return (!success || step > 2);
	}

	/**
	 * 第一步骤操作：将命令发送到的CALL站点
	 * @return 命令发送成功返回真，否则假
	 */
	private boolean send() {
		Regulate cmd = getCommand();
		Space space = cmd.getSpace();

		NodeSet set = getStaffPool().findTableSites(space);
		Node hub = ((set != null && set.size() > 0) ? set.next() : null);
		if (hub == null) {
			faultX(FaultTip.NOTFOUND_X, space); // "cannot be find '%s' site", space);
			return false;
		}

		// 发送到CALL站点，应答数据保存到内存
		boolean success = completeTo(hub, cmd);
		if (!success) {
			faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}
		return success;
	}

	/**
	 * 第二步骤操作：接收命令处理结果
	 * @return - 接收成功返回真，否则假
	 */
	private boolean receive() {
		int index = getEchoKeys().get(0);
		RegulateProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(RegulateProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		Regulate cmd = getCommand();
		boolean success = (product != null);
		if (success) {
			messageX(MessageTip.SUCCESSFUL_X, cmd.getPrimitive());
		} else {
			faultX(FaultTip.FAILED_X, cmd.getPrimitive());
		}

		// 返回结果
		return success;
	}

}