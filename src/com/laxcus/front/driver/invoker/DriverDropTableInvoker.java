/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 删表命令调用器
 * 
 * @author scott.liang
 * @version 1.1 11/02/2015
 * @since laxcus 1.0
 */
public class DriverDropTableInvoker extends DriverRuleInvoker {

	/** 操作步骤 **/
	private int step = 1;

	/**
	 * 构造默认的删表命令调用器，指定驱动任务
	 * @param mission 驱动任务
	 */
	public DriverDropTableInvoker(DriverMission mission) {
		super(mission);
		createRule();
	}

	/**
	 * 初始化事务规则
	 */
	private void createRule() {
		DropTable cmd = getCommand();
		// 定义规则
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(cmd.getSpace());
		addRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropTable getCommand() {
		return (DropTable) super.getCommand();
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
		// 不成功，或者大于2时，返回“真”退出
		return (!success || step > 2);
	}

	/**
	 * 处理第一阶段操作
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		DropTable cmd = getCommand();
		Space space = cmd.getSpace();
		// 判断数据表存在
		Table table = getStaffPool().findTable(space);
		if (table == null) {
			faultX(FaultTip.NOTFOUND_X, space);
			return false;
		}
		// 判断用户拥有删除表的权限
		boolean success = getStaffPool().canCreateTable(space);
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}
		// 通过AID站点转发到TOP站点
		return super.fireToHub();
	}

	/**
	 * 处理第二阶段操作
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		DropTableProduct product = null;
		// 判断结果是对象且成功
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null && product.isSuccessful());

		DropTable cmd = getCommand();
		Space space = cmd.getSpace();

		// 删除成功，更新记录
		if (success) {
			// 删除表及关联表的资源
			getStaffPool().dropTable(space);

//			// 有效的站点记录
//			List<Node> enables = getStaffPool().getGateways();
//			// 全部CALL站点记录
//			List<Node> all = CallOnFrontPool.getInstance().getHubs();
//
//			// 删除有效的，剩下是无效的
//			all.removeAll(enables);
//			// 注销这些无效站点
//			for (Node node : all) {
//				CallOnFrontPool.getInstance().logout(node);
//			}
//
//			// 通知DRIVER重新注册，以异步方式
//			getLauncher().checkin(false);

			// 设置结果
			setProduct(product);
		} else {
			// 异常
			faultX(FaultTip.FAILED_X, cmd);
		}

		return success;
	}

}
