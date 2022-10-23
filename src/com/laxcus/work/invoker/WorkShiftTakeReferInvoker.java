/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import com.laxcus.command.refer.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 获取账号的资源引用调用器<br>
 * 
 * @author scott.liang
 * @version 1.0 6/20/2019
 * @since laxcus 1.0
 */
public class WorkShiftTakeReferInvoker extends WorkInvoker {

	/**
	 * 构造获取账号的资源引用调用器，指定转发命令
	 * @param cmd 转发获取账号的资源引用命令
	 */
	public WorkShiftTakeReferInvoker(ShiftTakeRefer cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeRefer getCommand() {
		return (ShiftTakeRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeRefer shift = getCommand();
		// 发送到到HOME节点
		TakeRefer cmd = shift.getCommand();
		boolean success = launchToHub(cmd);
		// 不成功通知钩子退出
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
		TakeReferProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeReferProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);

		ShiftTakeRefer shift = getCommand();
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		return useful(success);
	}

}