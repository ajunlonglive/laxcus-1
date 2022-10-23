/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.account.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 获得签名的ACCOUNT站点的转发命令调用器。
 * 这个调用器是由客户端发出，目标是注册站点。
 * 
 * @author scott.liang
 * @version 1.1 7/28/2018
 * @since laxcus 1.0
 */
public class CommonShiftTakeSigerSiteInvoker extends CommonInvoker {

	/**
	 * 构造获得签名的ACCOUNT站点的转发命令调用器，指定转发命令
	 * @param shift 获得签名的ACCOUNT站点的转发命令
	 */
	public CommonShiftTakeSigerSiteInvoker(ShiftTakeSigerSite shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeSigerSite getCommand() {
		return (ShiftTakeSigerSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeSigerSite shift = getCommand();
		TakeSigerSite cmd = shift.getCommand();
		Node hub = getHub();

		// 发送到服务器
		boolean success = launchTo(hub, cmd);

		// 不成功，唤醒它！
		if (!success) {
			shift.getHook().done();
		}

		Logger.debug(this, "launch", success, "send to %s", hub);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftTakeSigerSite shift = getCommand();
		TakeSigerSiteHook hook = shift.getHook();

		TakeSigerSiteProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isObjectable(index)) {
				product = getObject(TakeSigerSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		}
		hook.done();

		return useful(success);
	}

}