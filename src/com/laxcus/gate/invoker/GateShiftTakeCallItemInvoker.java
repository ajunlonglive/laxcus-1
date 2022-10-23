/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.relate.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 获取CALL站点成员调用器。<br>
 * 
 * 命令发送给BANK站点，经BANK站点转发给TOP。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class GateShiftTakeCallItemInvoker extends GateInvoker {

	/**
	 * 构造获取CALL站点成员调用器，指定命令
	 * @param cmd 获取CALL站点成员
	 */
	public GateShiftTakeCallItemInvoker(ShiftTakeCallItem cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeCallItem getCommand() {
		return (ShiftTakeCallItem) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeCallItem shift = getCommand();
		TakeCallItem cmd = shift.getCommand();
		// 发送给BANK站点
		boolean success = launchToHub(cmd);
		// 不成功，通知等待的任务
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
		TakeCallItemProduct product = null;

		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeCallItemProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);
		ShiftTakeCallItem shift = getCommand();
		if (success) {
			shift.getHook().setResult(product);
		}
		// 唤醒钩子
		shift.getHook().done();

		return useful(success);
	}

}