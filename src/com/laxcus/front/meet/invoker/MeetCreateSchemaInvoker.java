/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.visit.*;

/**
 * 建立数据库调用器。<br><br>
 * 
 * 建立数据库只限普通注册用户且得到建库的授权。不允许管理员建库。<br><br>
 * 
 * 操作流程： <br>
 * 1. 去GATE站点锁定事务规则。<br>
 * 2. 向GATE站点发送建立数据库命令。<br>
 * 3. 从GATE站点接收建库结果（后续操作由GATE站点处理）。<br>
 * 4. 解除GATE站点上锁定事务。<br><br>
 * 
 * 
 * @author scott.liang
 * @version 1.22 9/18/2016
 * @since laxcus 1.0
 */
public class MeetCreateSchemaInvoker extends MeetRuleInvoker {

	/** 步骤，从1开始 **/
	private int step;

	/**
	 * 构造建立数据库命令的异步调用器，指定命令
	 * @param cmd 建立数据库命令
	 */
	public MeetCreateSchemaInvoker(CreateSchema cmd) {
		super(cmd, true); // 要求锁定资源的处理，防止FrontScheduleRefreshInvoker同步
		// 初始化事务规则
		initRule();
		// 从1开始
		step = 1;
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
	private void initRule() {
		CreateSchema cmd = getCommand();		
		// 定义规则
		SchemaRuleItem rule = new SchemaRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setFame(cmd.getSchema().getFame());
		addRule(rule);
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
			success = recieve();
			break;
		}
		// 自增1
		step++;
		// 大于2是完成，否则是没有完成
		return (!success || step > 2);
	}

	/**
	 * 发送命令到注册站点
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		// 发送到GATE站点
		return fireToHub();
	}

	/**
	 * 接收返回的处理结果
	 * @return 成功返回真，否则假
	 */
	private boolean recieve() {
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
			// 通知FRONT重新注册
			getLauncher().checkin(false);
		}

		// 显示处理结果
		print(success, cmd.getSchema().getFame());

		return success;
	}

	/**
	 * 显示处理结果
	 * @param success 成功
	 * @param fame 数据库名
	 */
	private void print(boolean success, Fame fame) {
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "CREATE-DATABASE/STATUS", "CREATE-DATABASE/DATABASE" });

		ShowItem showItem = new ShowItem();
		showItem.add(createConfirmTableCell(0, success));
		showItem.add(new ShowStringCell(1, fame));
		addShowItem(showItem);
		
		// 输出全部记录
		flushTable();

		if (!success) {
			String content = getXMLContent("CREATE-DATABASE/FAILED");
			fault(content, true);
			setSound(false);
		}
	}

}