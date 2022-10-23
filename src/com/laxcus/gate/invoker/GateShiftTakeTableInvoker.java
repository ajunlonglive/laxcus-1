/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 查表调用器。<br>
 * 
 * 命令转发给BANK站点，由BANK站点处理查找。
 * 
 * @author scott.liang
 * @version 1.0 8/7/2018
 * @since laxcus 1.0
 */
public class GateShiftTakeTableInvoker extends GateInvoker {

	/**
	 * 构造查表调用器，指定命令
	 * @param cmd 查表命令
	 */
	public GateShiftTakeTableInvoker(ShiftTakeTable cmd) {
		super(cmd);
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
		// 投递给BANK站点
		boolean success = launchToHub(cmd);
		// 不成功，通知退出！
		if (!success) {
			shift.getHook().done();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		Table table = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				table = getObject(Table.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShiftTakeTable shift = getCommand();

		// 判断有效
		boolean success = (table != null);
		if (success) {
			shift.getHook().setResult(table);
		}
		shift.getHook().done();

		return useful(success);
	}

}