/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import com.laxcus.command.access.permit.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 转发获取账号操作权级调用器
 * 
 * @author scott.liang
 * @version 1.0 5/31/2019
 * @since laxcus 1.0
 */
public class FrontShiftTakeGradeInvoker extends FrontInvoker {

	/**
	 * 构造转发获取账号操作权级调用器，指定命令
	 * @param cmd 转发获取账号操作权级调用器
	 */
	public FrontShiftTakeGradeInvoker(ShiftTakeGrade cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeGrade getCommand() {
		return (ShiftTakeGrade) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeGrade shift = getCommand();
		TakeGrade cmd = shift.getCommand();
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
		TakeGradeProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeGradeProduct.class, index);
				// grade = product.getGrade();
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		ShiftTakeGrade shift = getCommand();
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		return useful(success);
	}

}
