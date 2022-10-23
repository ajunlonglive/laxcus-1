/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 建立数据库调用器。<br><br>
 * 
 * 建库命令只限普通注册用户且得到建库的授权。管理员不能建库。建库操作基于事务。<br><br>
 * 
 * 操作流程： <br>
 * 1. FRONT通过AID向TOP发出命令。<br>
 * 2. TOP接受，判断注册用户权限，选择接受或者拒绝，并且返回结果。<br>
 * 3. FRONT接受返回结果，返回异常或者建成功提示<br>
 * 
 * @author scott.liang
 * @version 1.1 1/23/2016
 * @since laxcus 1.0
 */
public class DriverCreateSchemaInvoker extends DriverRuleInvoker {
	
	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造建立数据库调用器，指定任务
	 * @param mission 驱动任务
	 */
	public DriverCreateSchemaInvoker(DriverMission mission) {
		super(mission);
		// 建立事务
		createRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateSchema getCommand() {
		return (CreateSchema) super.getCommand();
	}

	/**
	 * 初始化事务规则
	 */
	private void createRule() {
		CreateSchema cmd = getCommand();		
		// 定义规则
		SchemaRuleItem rule = new SchemaRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setFame(cmd.getSchema().getFame());
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
		// 不成功，或者大于2，退出
		return (!success || step > 2);
	}

	/**
	 * 第一阶段操作
	 * @return
	 */
	private boolean send() {
		// 发送到AID站点
		return fireToHub();
	}

	/**
	 * 接收返回的处理结果
	 * @return
	 */
	private boolean receive() {
		// 判断处理结果
		CreateSchemaProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateSchemaProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null && product.isSuccessful());

		CreateSchema cmd = getCommand();
		// 如果成功，重新加载和显示资源
		if (success) {
			Schema schema = cmd.getSchema();
			// 通知资源管理池，更新CALL站点
			getStaffPool().createSchema(schema);
			
			// 通知DRIVER重新注册
			getLauncher().checkin(false);

			// 保存处理结果
			setProduct(product);
		} else {
			faultX(FaultTip.FAILED_X, cmd);
		}

		return success;
	}

}
