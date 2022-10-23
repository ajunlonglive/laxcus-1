/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.law.rule.*;
import com.laxcus.log.client.*;
import com.laxcus.util.display.show.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 删表命令调用器。<br><br>
 * 
 * 在获得删除权限的条件下，用户可以删除自己账号下的数据表。另：管理员拥有管理权限，但是不具备建库、删库、建表、删表能力。<br><br>
 * 
 * 删除操作流程：<br>
 * launch方法：<br>
 * 1. 判断表存在。<br>
 * 2. 拥有删除权限。<br>
 * 3. 通过AID站点转发命令到TOP站点。<br><br>
 * 
 * ending方法：<br>
 * 1. 成功，删除本地账号中的表和关联的CALL站点。<br>
 * 2. 失败，啥也不做，弹出错误提示。<br>
 * 
 * 1. TOP.launch检查账号拥有删除权限，许可发送命令到HOME站点，否则拒绝。
 * 2. TOP.ending等待HOME删除结果，返回删除结果给FRONT。
 * 
 * @author scott.liang
 * @version 1.1 3/23/2013
 * @since laxcus 1.0
 */
public class MeetDropTableInvoker extends MeetRuleInvoker {

	/** 操作步骤 **/
	private int step = 1;

	/**
	 * 构造删表命令调用器，指定删表命令
	 * @param cmd 删表命令
	 */
	public MeetDropTableInvoker(DropTable cmd) {
		super(cmd, true); // 锁定，防止FrontScheduleRefreshInvoker同步更新出错
		initRule();
	}

	/**
	 * 初始化事务规则
	 */
	private void initRule() {
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
	 * 发送命令到注册站点
	 * @return - 成功返回真，否则假
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
		boolean success = getStaffPool().canDropTable(space);
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}
		// 通过AID站点转发到TOP站点
		return super.fireToHub();
	}

	/**
	 * 从注册站点接受返回结果
	 * @return - 成功返回真，否则假
	 */
	private boolean receive() {
		DropTableProduct product = null;
		// 判断结果是对象且成功
		int index = findEchoKey(0);
		if(isSuccessObjectable(index)) {
			try {
				product = getObject(DropTableProduct.class, index);
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 判断成功
		boolean success = (product != null && product.isSuccessful());

		DropTable cmd = getCommand();
		Space space = cmd.getSpace();

		// 成功，删除本地仍然保存的表及关联CALL站点
		if (success) {
			getStaffPool().dropTable(space);
		}
		
		// 打印结果
		print(success, space);

		return success;
	}

	/**
	 * 显示处理结果
	 * @param success 成功或者否
	 * @param space 数据表名
	 */
	private void print(boolean success, Space space) {
		// 显示运行时间
		printRuntime();
		// 生成标题
		createShowTitle(new String[] { "DROP-TABLE/STATUS", "DROP-TABLE/TABLE" });
		ShowItem showItem = new ShowItem();
		showItem.add(createConfirmTableCell(0, success));
		showItem.add(new ShowStringCell(1, space));
		addShowItem(showItem);
		
		// 输出全部记录
		flushTable();
	}

}