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

/**
 * 获取系统管理员账号调用器。<br><br>
 * 
 * 管理员账号保存在BANK站点，GATE站点通过网络取得！
 * 
 * @author scott.liang
 * @version 1.0 6/28/2018
 * @since laxcus 1.0
 */
public class GateShiftTakeAdministratorInvoker extends GateInvoker {

	/**
	 * 构造获取系统管理员账号调用器，指定命令
	 * @param shift 获取系统管理员账号转发命令
	 */
	public GateShiftTakeAdministratorInvoker(ShiftTakeAdministrator shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeAdministrator getCommand() {
		return (ShiftTakeAdministrator) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeAdministrator shift = getCommand();
		TakeAdministrator cmd = shift.getCommand();
		// 发送到BANK站点
		boolean success = launchToHub(cmd);
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
		ShiftTakeAdministrator shift = getCommand();
		
		int index = findEchoKey(0);
		TakeAdministratorProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAdministratorProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		return useful(success);
	}

}
