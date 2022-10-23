/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.access.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 转发CAST DELETE调用器 <br>
 * 
 * 向DATA站点发送CAST DELETE命令，然后接收反馈。
 * 
 * @author scott.liang
 * @version 1.0 7/19/2014
 * @since laxcus 1.0
 */
public class CommonShiftCastDeleteInvoker extends CommonInvoker {

	/**
	 * 构造转发CAST DELETE调用器
	 * @param cmd CAST DELETE命令
	 */
	public CommonShiftCastDeleteInvoker(ShiftCastDelete cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftCastDelete getCommand() {
		return (ShiftCastDelete) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftCastDelete shift = getCommand();
		DeleteHook hook = shift.getHook();
		Node hub = shift.getHub();

		// 命令成员
		CommandItem item = new CommandItem(hub, shift.getCommand());
		// 发送命令到DATA节点
		boolean success = completeTo(item);
		if(!success) {
			hook.setFault(new EchoException("cannot be send to %s", hub));
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftCastDelete shift = getCommand();
		DeleteHook hook = shift.getHook();

		// 获得ASSUME DELETE实例
		int index = getEchoKeys().get(0);
		AssumeDelete assume = null;
		try {
			if (isSuccessObjectable(index)) {
				assume = getObject(AssumeDelete.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			hook.setFault(e);
			return false;
		}

		// 确认正确
		boolean success = (assume != null);
		if (success) {
			hook.setResult(assume);
		} else {
			hook.setFault(new EchoException("cannot be support!"));
		}
		// 返回结果
		return useful(success);
	}

}
