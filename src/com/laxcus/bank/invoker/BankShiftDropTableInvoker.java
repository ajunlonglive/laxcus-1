/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 删除数据表转发调用器。<br><br>
 * 
 * 当“BankCreateTableInvoker”建立数据表失败后，删除ACCOUNT站点上的数据表。
 * 
 * @author scott.liang
 * @version 1.0 7/7/2018
 * @since laxcus 1.0
 */
public class BankShiftDropTableInvoker extends BankInvoker {

	/**
	 * 构造删除数据表转发调用器，指定命令
	 * @param shift 转发申请用户数据表
	 */
	public BankShiftDropTableInvoker(ShiftDropTable shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftDropTable getCommand() {
		return (ShiftDropTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftDropTable shift = getCommand();
		DropTable cmd = shift.getCommand();
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
		ShiftDropTable shift = getCommand();
		int index = findEchoKey(0);
		DropTableProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropTableProduct.class, index);
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