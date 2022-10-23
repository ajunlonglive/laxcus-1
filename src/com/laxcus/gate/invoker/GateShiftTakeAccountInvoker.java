/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.access.account.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 申请用户账号转发调用器。<br><br>
 * 
 * BANK/GATE/ENTRANCE站点发出，目标是HASH站点。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class GateShiftTakeAccountInvoker extends GateInvoker {

	/**
	 * 构造申请用户账号转发调用器，指定命令
	 * @param shift 转发申请用户账号
	 */
	public GateShiftTakeAccountInvoker(ShiftTakeAccount shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeAccount getCommand() {
		return (ShiftTakeAccount) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeAccount shift = getCommand();
		TakeAccount cmd = shift.getCommand();
		Node account = shift.getRemote();
		// 发送到ACCOUNT站点
		boolean success = launchTo(account, cmd);
		// 失败，通知命令钩子
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
		TakeAccountProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAccountProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);
		
		ShiftTakeAccount shift = getCommand();
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		return useful(success);
	}

}