/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import java.util.*;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.front.pool.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 建表命令调用器
 * 
 * @author scott.liang
 * @version 1.1 11/02/2015
 * @since laxcus 1.0
 */
public class DriverCreateTableInvoker extends DriverRuleInvoker {

	/** 步骤 **/
	private int step = 1;

	/**
	 * 构造建表命令调用器，指定任务
	 * @param mission 驱动任务
	 */
	public DriverCreateTableInvoker(DriverMission mission) {
		super(mission);
		// 建立事务
		createRule();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateTable getCommand() {
		return (CreateTable) super.getCommand();
	}

	/**
	 * 建立事务规则
	 */
	private void createRule() {
		CreateTable cmd = getCommand();		
		// 定义规则
		TableRuleItem rule = new TableRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setSpace(cmd.getSpace());
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
		// 不成功，或者大于2，返回“真”退出。
		return (!success || step > 2);
	}

	/**
	 * 发送命令到注册站点
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		return super.fireToHub();
	}

	/**
	 * 接收建表结果
	 * @return 成功返回真，否则假
	 */
	private boolean receive() {
		CreateTableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = super.getObject(CreateTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		CreateTable cmd = getCommand();
		Table table = cmd.getTable();
		Space space = table.getSpace();

		Logger.debug(this, "receive", success, "creata table %s", space);

		if (success) {
			// 注册到CALL站点
			List<GatewayNode> list = product.list();
			for (GatewayNode gateway : list) {
				Logger.info(this, "receive", "\'%s\' table site is %s", space, gateway);
				
				// 经过GATE站点处理，此时的公网/内网一样，随便取一个
				Node hub = gateway.getPublic();
				// 增加FRONT -> CALL站点映射
				checkPock(hub);

				// 判断地址是否存在，存在，保存地址；地址不存在，注册再保存址！
				boolean exists = CallOnFrontPool.getInstance().contains(hub);
				if (exists) {
					getStaffPool().addTableSite(hub, space);
				} else {
					// 注册到CALL站点
					boolean logined = CallOnFrontPool.getInstance().login(hub);
					// 注册成功，保存数据表名
					if (logined) {
						getStaffPool().addTableSite(hub, space);
					}
				}
			}
			// 保存表
			getStaffPool().createTable(table);

			// 通知DRIVER重新注册，采用异步方式
			getLauncher().checkin(false);

			// 设置结果
			setProduct(product); 
		} else {
			// 异常
			faultX(FaultTip.FAILED_X, cmd);
		}

		return success;
	}

}
