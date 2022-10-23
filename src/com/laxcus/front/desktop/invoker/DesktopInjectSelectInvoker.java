/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.access.*;
import com.laxcus.command.conduct.*;
import com.laxcus.distribute.conduct.*;
import com.laxcus.util.naming.*;
import com.laxcus.util.tip.*;

/**
 * INJECT SELECT命令调用器。<BR><BR>
 * 
 * 这只是一个中继调用器，它需要生成Conduct命令，以分布计算的方式处理数据的查询和之后的插入。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopInjectSelectInvoker extends DesktopInvoker {

	/**
	 * 构造SELECT命令调用器，指定SELECT命令
	 * @param cmd SELECT命令
	 */
	public DesktopInjectSelectInvoker(InjectSelect cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public InjectSelect getCommand() {
		return (InjectSelect) super.getCommand();
	}


	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 插入数据
		boolean success = this.createInjectSelect();

		// 如果失败，弹出错误提示
		if (!success) {
			InjectSelect cmd = getCommand();
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
		InjectSelect cmd = getCommand();
		Phase phase = new Phase(getUsername(), PhaseTag.INIT, root);
		InitObject initObject = new InitObject(phase);
		initObject.addCommand("INJECT_SELECT_OBJECT", cmd); // INJECT SELECT命令保存到自定义参数

		// 将规则写入INIT对象实例，DesktopConductInvoker会从Conduct命令读取它们
		initObject.addRules(cmd.getRules());

		// 构造分布计算实例
		Conduct conduct = new Conduct(root);
		// 设置初始化命名对象，数据资源的处理，如参数分配、数据分片等，到CALL.INIT上执行
		conduct.setInitObject(initObject);

		// 提交给命令管理池处理
		return getCommandPool().press(conduct, getDisplay());
	}

	/**
	 * 建立一个查询插入的CONDUCT调用器。当前调用器退出。
	 * @return 成功返回真，否则假。
	 */
	private boolean createInjectSelect() {
		// INJECT_SELECT是根命名
		Sock root = Sock.doSystemSock("INJECT_SELECT");
		return shift(root);
	}

}