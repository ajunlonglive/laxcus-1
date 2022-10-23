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
 * 设置命令处理模式调用器
 * 
 * @author scott.liang
 * @version 1.0 09/09/2015
 * @since laxcus 1.0
 */
public class DriverCommandModeInvoker extends DriverInvoker {

	/**
	 * 构造设置命令处理模式调用器
	 * @param mission 驱动任务
	 */
	public DriverCommandModeInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CommandMode getCommand() {
		return (CommandMode) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CommandMode cmd = getCommand();

		FrontLauncher launcher = getLauncher();
		launcher.setMemory(cmd.isMemoryMode());

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