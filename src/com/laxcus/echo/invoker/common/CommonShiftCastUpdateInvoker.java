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
 * 转发CAST UPDATE调用器 <br>
 * 
 * 向DATA站点发送CAST UPDATE命令，然后接收反馈。
 * 
 * @author scott.liang
 * @version 1.0 7/19/2014
 * @since laxcus 1.0
 */
public class CommonShiftCastUpdateInvoker extends CommonInvoker {

	/**
	 * 构造转发CAST UPDATE调用器，指定转发命令
	 * @param shift CAST UPDATE转发命令
	 */
	public CommonShiftCastUpdateInvoker(ShiftCastUpdate shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftCastUpdate getCommand() {
		return (ShiftCastUpdate) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftCastUpdate shift = getCommand();
		UpdateHook hook = shift.getHook();

		Node hub = shift.getHub();

		CommandItem item = new CommandItem(hub, shift.getCommand());
		// 发送到目标站点
		boolean success = completeTo(item);
		if (!success) {
			hook.setFault(new EchoException("cannot be send to %s", hub));
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftCastUpdate shift = getCommand();
		UpdateHook hook = shift.getHook();

		// 获得ASSUME UPDATE实例
		int index = getEchoKeys().get(0);
		AssumeUpdate assume = null;
		try {
			if (isSuccessObjectable(index)) {
				assume = getObject(AssumeUpdate.class, index);
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
