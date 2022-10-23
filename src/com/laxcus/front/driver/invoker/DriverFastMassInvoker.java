/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.fast.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.set.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 数据块处理命令调用器
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus 1.0
 */
public class DriverFastMassInvoker extends DriverRuleInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造数据块处理命令调用器，指定驱动任务
	 * @param mission 驱动任务
	 */
	protected DriverFastMassInvoker(DriverMission mission) {
		super(mission);
		createRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public FastMass getCommand() {
		return (FastMass) super.getCommand();
	}

	/**
	 * 定义事务处理规则
	 */
	private void createRule() {
		FastMass cmd = getCommand();
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(cmd.getSpace());
		// 保存事务规则
		super.addRule(rule);
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
		// 不成功退出，或者超过2是完成退出
		return (!success || step > 2);
	}

	/**
	 * 将命令发送到CALL站点
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		FastMass cmd = getCommand();
		Space space = cmd.getSpace();
		// 1. 找到一个CALL站点，把命令发给它。
		NodeSet set = getStaffPool().findTableSites(space);
		// 顺序选择，保持调用站点的分配平衡
		Node hub = (set != null ? set.next() : null);
		if (hub == null) {
			super.fault("cannot be find '%s' site", space);
			return false;
		}
		// 发送命令到服务器
		boolean success = completeTo(hub, cmd);
		if (!success) {
			super.faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		return success;
	}

	/**
	 * 接受来自CALL站点的反馈
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		FastMassProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(FastMassProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null);

		if (success) {
			setProduct(product);
		} else {
			FastMass cmd = getCommand();
			faultX(FaultTip.FAILED_X, cmd);
		}

		return success;
	}

}