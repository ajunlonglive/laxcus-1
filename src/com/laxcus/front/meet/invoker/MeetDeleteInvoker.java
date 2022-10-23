/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * DELETE命令调用器。<BR><BR>
 * 
 * @author scott.liang
 * @version 1.1 09/23/2013
 * @since laxcus 1.0
 */
public class MeetDeleteInvoker extends MeetQueryInvoker {

	/**
	 * 构造DELETE命令调用器，指定DELETE命令
	 * @param cmd DELETE命令
	 */
	public MeetDeleteInvoker(Delete cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Delete getCommand() {
		return (Delete) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		boolean success = false;

		Delete delete = getCommand();
		// 判断是带嵌套的删除操作
		if (delete.hasNested()) {
			success = createSubDelete();
		} else {
			success = createDirectDelete();
		}

		// 如果失败，弹出错误提示
		if (!success) {
			faultX(FaultTip.FAILED_X, delete);
		}

		// 退出
		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 建立一个简单的DELETE，交给调用器处理
	 * @return 成功返回真，否则假
	 */
	private boolean createDirectDelete() {
		Delete delete = getCommand();
		MeetDirectDeleteInvoker invoker = new MeetDirectDeleteInvoker(delete);
		return getInvokerPool().launch(invoker);
	}

	/**
	 * 根据INIT阶段命名，生成CONDUCT异步调用器，转发执行。
	 * @param root 根命名
	 * @return 成功受理返回“真”，否则“假”。
	 */
	private boolean shift(Sock root) {
		Delete cmd = getCommand();
		Phase phase = new Phase(getUsername(), PhaseTag.INIT, root);
		InitObject initObject = new InitObject(phase);
		initObject.addCommand("DELETE_OBJECT", cmd);

		//		// 互斥写。在执行过程中，嵌套查询中的表要锁定
		//		TableRule rule = new TableRule(RuleOperator.EXCLUSIVE_WRITE);
		//		rule.setSpace(cmd.getSpace());
		//		// 收集表名
		//		collect(rule, cmd.getWhere());
		//		// 保存规则
		//		initObject.addRule(rule);

		// 独享资源写操作
//		List<RuleItem> rules = super.createTableRules(cmd, RuleOperator.EXCLUSIVE_WRITE);
		
		// 保存DELETE独享资源写操作
		initObject.addRules(cmd.getRules());

		// 构造分布计算实例
		Conduct conduct = new Conduct(root);
		// 设置初始化命名对象，数据资源的处理，如参数分配、数据分片等，到CALL.INIT上执行
		conduct.setInitObject(initObject);

		// 提交给命令管理池处理
		return getCommandPool().press(conduct);
	}

	/**
	 * 建立一个嵌套删除的CONDUCT调用器，提交给命令管理池后，当前调用器退出。
	 * @return 成功返回真，否则假。
	 */
	private boolean createSubDelete() {
		// SUBDELETE是系统嵌套删除的根命名
		Sock root = Sock.doSystemSock("SUBDELETE");
		return shift(root);
	}
}
