/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 诊断用户账号存在调用器。
 * 
 * @author scott.liang
 * @version 1.0 7/09/2015
 * @since laxcus 1.0
 */
public class MeetShiftAssertUserInvoker extends MeetInvoker {

	/**
	 * 构造诊断用户账号存在调用器，指定命令
	 * @param cmd 诊断用户账号存在命令
	 */
	public MeetShiftAssertUserInvoker(ShiftAssertUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftAssertUser getCommand() {
		return (ShiftAssertUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftAssertUser shift = getCommand();
		AssertUser cmd = shift.getCommand();

		// 投递到GATE站点
		Node hub = getHub();
		boolean success = launchTo(hub, cmd);
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
		int index = findEchoKey(0);
		AssertUserProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(AssertUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShiftAssertUser shift = getCommand();
		boolean success = (product != null);
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		// 退出
		return useful(success);
	}

}