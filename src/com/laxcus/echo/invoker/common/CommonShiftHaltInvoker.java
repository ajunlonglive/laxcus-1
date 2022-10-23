/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.halt.*;
import com.laxcus.log.client.*;
import com.laxcus.site.Node;

/**
 * 转发中断命令调用器
 * 
 * 这个调用器向指定目标地址发送中断命令，发送完即退出，不等待反馈结果。
 * 
 * @author scott.liang
 * @version 1.0 7/12/2013
 * @since laxcus 1.0
 */
public class CommonShiftHaltInvoker extends CommonInvoker {

	/**
	 * 构造转发中断命令调用器，指定转发命令
	 * @param cmd 转发中断命令
	 */
	public CommonShiftHaltInvoker(ShiftHalt cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftHalt getCommand() {
		return (ShiftHalt) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftHalt shift = getCommand();
		Halt cmd = shift.getCommand();
		Node[] sites = shift.getSites();

		int count = super.directTo(sites, cmd);
		boolean success = (count > 0);

		Logger.debug(this, "launch", success, "send count %d", count);

		return useful(success);
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
