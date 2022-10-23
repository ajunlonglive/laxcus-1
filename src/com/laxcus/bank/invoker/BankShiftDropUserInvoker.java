/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 删除账号转发调用器。<br><br>
 * 
 * 当“BankSerialShiftCreateUserInvoker”建立账号失败后，删除ACCOUNT站点上的账号。
 * 
 * @author scott.liang
 * @version 1.0 7/7/2018
 * @since laxcus 1.0
 */
public class BankShiftDropUserInvoker extends BankInvoker {

	/**
	 * 构造删除账号转发调用器，指定命令
	 * @param shift 转发申请用户账号
	 */
	public BankShiftDropUserInvoker(ShiftDropUser shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftDropUser getCommand() {
		return (ShiftDropUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftDropUser shift = getCommand();
		DropUser cmd = shift.getCommand();
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
		ShiftDropUser shift = getCommand();
		int index = findEchoKey(0);
		DropUserProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropUserProduct.class, index);
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