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
 * UPDATE命令调用器。<BR><BR>
 * 
 * @author scott.liang
 * @version 1.1 09/23/2013
 * @since laxcus 1.0
 */
public class MeetUpdateInvoker extends MeetQueryInvoker {

	/**
	 * 构造UPDATE命令调用器，指定UPDATE命令
	 * @param cmd - UPDATE命令
	 */
	public MeetUpdateInvoker(Update cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Update getCommand() {
		return (Update) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		boolean success = false;

		Update update = getCommand();
		if (update.hasNested()) {
			success = createSubUpdate();
		} else {
			success = createDirectUpdate();
		}

		// 如果失败，弹出错误提示
		if (!success) {
			faultX(FaultTip.FAILED_X, update);
		}

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
	 * 建立一个简单的UPDATE检索，交给调用器处理
	 * @return 成功返回真，否则假
	 */
	private boolean createDirectUpdate() {
		Update update = getCommand();
		MeetDirectUpdateInvoker invoker = new MeetDirectUpdateInvoker(update);
		return getInvokerPool().launch(invoker);
	}

	/**
	 * 建立一个嵌套更新的CONDUCT调用器，提交给命令管理池后，当前调用器退出。
	 * @return 成功返回真，否则假。
	 */
	private boolean createSubUpdate() {
		// SUBUPDATE是系统嵌套更新的根命名
		Sock root = Sock.doSystemSock("SUBUPDATE");
		return shift(root);
	}

	/**
	 * 根据INIT阶段命名，生成CONDUCT异步调用器，转发执行。
	 * @param root 根命名
	 * @return 成功受理返回“真”，否则“假”。
	 */
	private boolean shift(Sock root) {
		Update cmd = getCommand();
		Phase phase = new Phase(getUsername(), PhaseTag.INIT, root);
		InitObject initObject = new InitObject(phase);
		initObject.addCommand("UPDATE_OBJECT", cmd);

		//		// 互斥写。在执行过程中，嵌套更新中的表要锁定
		//		TableRule rule = new TableRule(RuleOperator.EXCLUSIVE_WRITE);
		//		rule.setSpace(cmd.getSpace());
		//		// 收集表名
		//		collect(rule, cmd.getWhere());

		// 以独享写方式，生成和保存表规则
//		List<RuleItem> rules = createTableRules(cmd, RuleOperator.EXCLUSIVE_WRITE);
		
		// 保存UPDATE命令的事务规则
		initObject.addRules(cmd.getRules());

		// 构造分布计算实例
		Conduct conduct = new Conduct(root);
		// 设置初始化命名对象，数据资源的处理，如参数分配、数据分片等，到CALL.INIT上执行
		conduct.setInitObject(initObject);

		// 提交给命令管理池处理
		return getCommandPool().press(conduct);
	}
}