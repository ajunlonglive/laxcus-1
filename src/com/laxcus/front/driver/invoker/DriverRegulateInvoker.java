/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.rebuild.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 数据优化调用器
 * 
 * @author scott.liang
 * @version 1.0 11/20/2013
 * @since laxcus 1.0
 */
public class DriverRegulateInvoker extends DriverRuleInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造数据优化调用器，指定驱动任务
	 * @param mission 驱动任务
	 */
	public DriverRegulateInvoker(DriverMission mission) {
		super(mission);
		createRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Regulate getCommand() {
		return (Regulate) super.getCommand();
	}

	/**
	 * 设置事务规则
	 */
	private void createRule() {
		Regulate cmd = getCommand();
		Space space = cmd.getSpace();
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(space);
		addRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.driver.invoker.DriverRuleInvoker#process()
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
			super.fault("cannot be find '%s' site", space);
			return false;
		}

		// 发送命令到CALL站点，再由CALL转发到DATA主站点
		boolean success = completeTo(hub, cmd);
		if (!success) {
			super.fault("cannot be submit %s", hub);
		}
		return success;
	}

	/**
	 * 第二步，接收CALL返回的处理结果
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		int index = findEchoKey(0);
		RegulateProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(RegulateProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			super.fault(e);
			return false;
		}

		// 判断成功
		boolean success = (product != null);
		if (success) {
			setProduct(product);
		} else {
			Regulate cmd = getCommand();
			faultX(FaultTip.ILLEGAL_COMMAND, cmd);
		}

		// 返回结果
		return success;
	}

}
