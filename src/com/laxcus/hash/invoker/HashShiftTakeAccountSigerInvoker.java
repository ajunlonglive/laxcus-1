/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.invoker;

import com.laxcus.command.site.bank.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 获得坐标范围内账号转发命令调用器
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class HashShiftTakeAccountSigerInvoker extends HashInvoker {

	/**
	 * 构造获得坐标范围内账号转发命令调用器，指定命令
	 * @param cmd 获得坐标范围内账号转发命令
	 */
	public HashShiftTakeAccountSigerInvoker(ShiftTakeAccountSiger cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeAccountSiger getCommand(){
		return (ShiftTakeAccountSiger)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeAccountSiger shift = getCommand();
		TakeAccountSiger cmd = shift.getCommand();
		Node account = shift.getRemote();

		// 发送给ACCOUNT站点
		boolean success = launchTo(account, cmd);
		// 不成功，通知等待线程
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
		ShiftTakeAccountSiger shift = getCommand();
		TakeAccountSigerHook hook = shift.getHook();

		int index = findEchoKey(0);
		TakeAccountSigerProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAccountSigerProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功，保存参数
		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		}
		// 唤醒
		hook.done();
		// 退出
		return useful(success);
	}

}
