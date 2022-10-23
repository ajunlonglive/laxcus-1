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
 * 向BANK站点申请主机序列号转发调用器。<br><br>
 * 
 * 这个调用器由HASH/GATE发出，目标是BANK站点。
 * 
 * @author scott.liang
 * @version 1.0 6/27/2018
 * @since laxcus 1.0
 */
public class CommonShiftTakeSiteSerialInvoker extends CommonInvoker {

	/**
	 * 构造向BANK站点申请主机序列号转发调用器，指定命令
	 * @param shift 转发向BANK站点申请主机序列号
	 */
	public CommonShiftTakeSiteSerialInvoker(ShiftTakeSiteSerial shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeSiteSerial getCommand() {
		return (ShiftTakeSiteSerial) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeSiteSerial shift = getCommand();
		TakeSiteSerial cmd = shift.getCommand();
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
		ShiftTakeSiteSerial shift = getCommand();
		int index = findEchoKey(0);
		TakeSiteSerialProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeSiteSerialProduct.class, index);
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
