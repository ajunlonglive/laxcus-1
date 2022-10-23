/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.site.bank.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 获得BANK子站点数目数目转发调用器。<br><br>
 * 
 * 这个调用器由HASH/GATE发出，目标是BANK站点。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class CommonShiftTakeBankSubSiteCountInvoker extends CommonInvoker {

	/**
	 * 构造获得BANK子站点数目数目转发调用器，指定命令
	 * @param shift 转发获得BANK子站点数目数目
	 */
	public CommonShiftTakeBankSubSiteCountInvoker(ShiftTakeBankSubSiteCount shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeBankSubSiteCount getCommand() {
		return (ShiftTakeBankSubSiteCount) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeBankSubSiteCount shift = getCommand();
		TakeBankSubSiteCount cmd = shift.getCommand();
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
		ShiftTakeBankSubSiteCount shift = getCommand();
		int index = findEchoKey(0);
		TakeBankSubSiteCountProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeBankSubSiteCountProduct.class, index);
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
