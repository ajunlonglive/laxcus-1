/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 查表转发命令调用器。<br>
 * 这个调用器被所有站点使用。
 * 
 * @author scott.liang
 * @version 1.0 12/09/2011
 * @since laxcus 1.0
 */
public final class CommonShiftTakeTableInvoker extends CommonInvoker {

	/**
	 * 构造查表转发命令调用器，指定转发命令
	 * @param shift 转发命令
	 */
	public CommonShiftTakeTableInvoker(ShiftTakeTable shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeTable getCommand() {
		return (ShiftTakeTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeTable shift = getCommand();
		TakeTable cmd = shift.getCommand();

		// 发送命令到目标站点
		boolean success = launchToHub(cmd);
		// 不成功唤醒命令钩子退出
		if (!success) {
			shift.getHook().done();
		}

		Logger.debug(this, "launch", success, "submit '%s' to %s", cmd.getSpace(), getHub());

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftTakeTable shift = getCommand();
		TakeTableHook hook = shift.getHook();

		Table table = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				table = getObject(Table.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (table != null);
		if (success) {
			hook.setResult(table);
		}
		hook.done();

		Logger.debug(this, "ending", success, "take '%s'", shift.getCommand().getSpace());

		return useful(success);
	}

}
