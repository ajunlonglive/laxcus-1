/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.access.parse.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.schema.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 删除数据库命令异步调用器。<br>
 * 删除一个数据库，同时删除它下属的所有表。<br>
 * 
 * @author scott.liang
 * @version 1.2 7/23/2013
 * @since laxcus 1.0
 */
public class MeetDropSchemaInvoker extends MeetRuleInvoker {

	/** 处理步骤 **/
	private int step = 1;

	/**
	 * 构造删除数据库命令异步调用器，指定删除数据库命令
	 * @param cmd 删除数据库命令
	 */
	public MeetDropSchemaInvoker(DropSchema cmd) {
		super(cmd, true); // 锁定，防止FrontScheduleRefreshInvoker同时更新
		initRule();
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
	private void initRule() {
		DropSchema cmd = getCommand();		
		// 定义规则
		SchemaRuleItem rule = new SchemaRuleItem(RuleOperator.EXCLUSIVE_WRITE);
		rule.setFame(cmd.getFame());
		addRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.front.meet.invoker.MeetRuleInvoker#process()
	 */
	@Override
	protected boolean process() {
		// 如果是管理员，拒绝执行
		if (isAdministrator()) {
			faultX(FaultTip.PERMISSION_MISSING);
			return true;
		}
	
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
		// 大于2是完成，否则是没有完成
		return (!success || step > 2);
	}

	/**
	 * 处理第一阶段
	 * @return 成功返回真，否则假
	 */
	private boolean send() {
		// 检查注册用户有删除的权限
		DropSchema cmd = getCommand();
		Fame fame = cmd.getFame();
		// 1. 判断数据库存在
		boolean success = false;
		try {
			success = getStaffPool().hasSchema(fame);
		} catch (ResourceException e) {
			Logger.error(e);
		}
		if (!success) {
			faultX(FaultTip.NOTFOUND_X, fame);
			return false;
		}
		// 2. 判断拥有删除数据库权限
		success = getStaffPool().canDropSchema(fame);
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}

		// 发送命令到服务器
		return fireToHub();
	}

	/**
	 * 处理第二阶段
	 * @return - 成功返回真，否则假
	 */
	private boolean receive() {
		// 判断处理结果
		DropSchemaProduct product = null;
		
		int index = getEchoKeys().get(0);
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
		Fame fame = cmd.getFame();
		if (success) {
			// 删除数据库
			getStaffPool().dropSchema(fame);
		}

		// 打印结果
		print(success, fame);

		// 返回结果
		return success;
	}

	
	/**
	 * 显示处理结果
	 * @param success 成功或者否
	 * @param fame 数据库名
	 */
	private void print(boolean success, Fame fame) {
		// 显示运行时间
		printRuntime();

		// 生成标题
		createShowTitle(new String[] { "DROP-DATABASE/STATUS", "DROP-DATABASE/DATABASE" });

		ShowItem showItem = new ShowItem();
		showItem.add(createConfirmTableCell(0, success));
		showItem.add(new ShowStringCell(1, fame));
		addShowItem(showItem);
		
		// 输出全部记录
		flushTable();
	}
}