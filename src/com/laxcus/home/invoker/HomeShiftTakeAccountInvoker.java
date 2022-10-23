/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.access.account.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 获取账号调用器<br>
 * 
 * @author scott.liang
 * @version 1.0 7/9/2018
 * @since laxcus 1.0
 */
public class HomeShiftTakeAccountInvoker extends HomeInvoker {

	/**
	 * 构造获取账号调用器，指定转发命令
	 * @param cmd 转发获取账号命令
	 */
	public HomeShiftTakeAccountInvoker(ShiftTakeAccount cmd) {
		super(cmd);
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
		// 判断定义了ACCOUNT站点，如果没有拿出TOP站点
		Node hub = shift.getRemote();
		if (hub == null) {
			hub = getHub();
		}

		// 发送到ACCOUNT或者TOP站点
		TakeAccount cmd = shift.getCommand();
		boolean success = launchTo(hub, cmd);
		// 不成功通知钩子退出
		if (!success) {
			shift.getHook().done();
		}

		Logger.debug(this, "launch", success, "result is");

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
