/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import com.laxcus.command.relate.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 转发获得账号所有人的CALL站点调用器
 * 
 * @author scott.liang
 * @version 1.0 5/31/2019
 * @since laxcus 1.0
 */
public class FrontShiftTakeOwnerCallInvoker extends FrontInvoker {

	/**
	 * 构造转发获得账号所有人的CALL站点，指定命令
	 * @param cmd 转发获得账号所有人的CALL站点
	 */
	public FrontShiftTakeOwnerCallInvoker(ShiftTakeOwnerCall cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeOwnerCall getCommand() {
		return (ShiftTakeOwnerCall) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeOwnerCall shift = getCommand();
		TakeOwnerCall cmd = shift.getCommand();
		boolean success = launchToHub(cmd);
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
		TakeOwnerCallProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeOwnerCallProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		ShiftTakeOwnerCall shift = getCommand();
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		return useful(success);
	}

}