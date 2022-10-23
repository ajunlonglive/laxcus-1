/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.site.find.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 查询用户签名站点命令调用器
 * 
 * @author scott.liang
 * @version 1.0 5/28/2012
 * @since laxcus 1.0
 */
public final class CommonShiftFindUserSiteInvoker extends CommonInvoker {

	/**
	 * 构造查询用户签名站点命令调用器，指定命令
	 * @param cmd
	 */
	public CommonShiftFindUserSiteInvoker(ShiftFindUserSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftFindUserSite getCommand() {
		return (ShiftFindUserSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftFindUserSite shift = getCommand();
		FindUserSite cmd = shift.getCommand();
		Node hub = shift.getHub();

		boolean success = completeTo(hub, cmd);
		if (!success) {
			FindUserSiteHook hook = shift.getHook();
			hook.done();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftFindUserSite shift = getCommand();
		FindUserSiteHook hook = shift.getHook();

		FindUserSiteProduct product = null;
		try {
			if (isSuccessObjectable(0)) {
				product = getObject(FindUserSiteProduct.class, 0);
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