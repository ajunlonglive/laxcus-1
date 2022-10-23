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
 * SQL连接查询
 *
 * @author scott.liang
 * @version 1.0 12/22/2020
 * @since laxcus 1.0
 */
public class MeetJoinInvoker extends MeetInvoker {

	/**
	 * 构造默认的SQL连接查询
	 * @param cmd
	 */
	public MeetJoinInvoker(Join cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Join getCommand() {
		return (Join) super.getCommand();
	}
	
		/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 插入数据
		boolean success = this.createJoin();

		// 如果失败，弹出错误提示
		if (!success) {
			Join cmd = getCommand();
			faultX(FaultTip.FAILED_X, cmd);
		}

		return useful(success);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}

	/**
	 * 根据INIT阶段命名，生成CONDUCT异步调用器，转发执行。
	 * @param root 根命名
	 * @return 成功受理返回“真”，否则“假”。
	 */
	private boolean shift(Sock root) {
		Join cmd = getCommand();
		Phase phase = new Phase(getUsername(), PhaseTag.INIT, root);
		InitObject initObject = new InitObject(phase);
		initObject.addCommand("JOIN_SELECT_OBJECT", cmd); // JOIN SELECT命令保存到自定义参数

		// 将规则写入INIT对象实例，MeetConductInvoker会从Conduct命令读取它们
		initObject.addRules(cmd.getRules());

		// 构造分布计算实例
		Conduct conduct = new Conduct(root);
		// 设置初始化命名对象，数据资源的处理，如参数分配、数据分片等，到CALL.INIT上执行
		conduct.setInitObject(initObject);

		// 提交给命令管理池处理
		return getCommandPool().press(conduct);
	}

	/**
	 * 建立一个连接查询的CONDUCT调用器。当前调用器退出。
	 * @return 成功返回真，否则假。
	 */
	private boolean createJoin() {
		// JOIN_SELECT是根命名
		Sock root = Sock.doSystemSock("JOIN_SELECT");
		return shift(root);
	}

}
