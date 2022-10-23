/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.mix.*;
import com.laxcus.echo.product.*;
import com.laxcus.front.*;
import com.laxcus.front.driver.mission.*;

/**
 * 设置命令超时调用器
 * 
 * @author scott.liang
 * @version 1.0 5/23/2016
 * @since laxcus 1.0
 */
public class DriverCommandTimeoutInvoker extends DriverInvoker {

	/**
	 * 构造设置命令超时调用器
	 * @param mission 驱动任务
	 */
	public DriverCommandTimeoutInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CommandTimeout getCommand() {
		return (CommandTimeout) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CommandTimeout cmd = getCommand();

		FrontLauncher launcher = getLauncher();
		launcher.setCommandTimeout(cmd.getInterval());

		// 设置回显报告
		setProduct(new ConfirmProduct(true));

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}
