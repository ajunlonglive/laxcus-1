/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.access.parse.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 删除数据库调用器。<br>
 * 删除一个数据库，同时删除它下属的所有表。<br>
 * 
 * @author scott.liang
 * @version 1.2 7/23/2013
 * @since laxcus 1.0
 */
public class DriverDropSchemaInvoker extends DriverRuleInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造删除数据库命令异步调用器，指定删除数据库命令
	 * @param mission 删除数据库命令
	 */
	public DriverDropSchemaInvoker(DriverMission mission) {
		super(mission);
		createRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropSchema getCommand() {
		return (DropSchema) super.getCommand();
	}

	/**
	 * 初始化事务规则
	 */
	private void createRule() {
		DropSchema cmd = getCommand();		
		// 定义规则
		SchemaRuleItem rule = new SchemaRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setFame(cmd.getFame());
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
		// 不成功，或者大于2，退出！
		return (!success || step > 2);
	}

	/**
	 * 处理第一阶段
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		// 检查注册用户有删除的权限
		DropSchema cmd = getCommand();
		Fame name = cmd.getFame();
		// 1. 判断数据库存在
		boolean success = false;
		try {
			success = getStaffPool().hasSchema(name);
		} catch (ResourceException e) {
			Logger.error(e);
		}
		if (!success) {
			faultX(FaultTip.NOTFOUND_X, name);
			return false;
		}
		// 2. 判断拥有删除数据库权限
		success = getStaffPool().canDropSchema(name);
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING); //权限不足
			return false;
		}

		// 发送命令到服务器
		return fireToHub();
	}

	/**
	 * 处理第二阶段
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		// 判断处理结果
		DropSchemaProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropSchemaProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null && product.isSuccessful());

		DropSchema cmd = getCommand();
		Fame name = cmd.getFame();
		if (success) {
			// 删除数据库
			getStaffPool().dropSchema(name);

//			// 有效的站点记录
//			List<Node> enables = getStaffPool().getGateways();
//			// 全部CALL站点记录
//			List<Node> all = CallOnFrontPool.getInstance().getHubs();
//			// 删除有效的，剩下是无效的
//			all.removeAll(enables);
//			// 注销这些无效站点
//			for(Node node : all) {
//				CallOnFrontPool.getInstance().logout(node);
//			}
//			// 通知DRIVER重新注册，以异步方式
//			getLauncher().checkin(false);

			// 设置返回结果
			setProduct(product);
		} else {
			faultX(FaultTip.FAILED_X, cmd);
		}
		// 返回结果
		return success;
	}

}