/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 诊断数据表存在调用器。
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopShiftAssertTableInvoker extends DesktopInvoker {

	/**
	 * 构造诊断数据表存在调用器，指定命令
	 * @param cmd 诊断数据表存在命令
	 */
	public DesktopShiftAssertTableInvoker(ShiftAssertTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftAssertTable getCommand() {
		return (ShiftAssertTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftAssertTable shift = getCommand();
		AssertTable cmd = shift.getCommand();

		// 投递到GATE站点
		Node hub = getHub();
		boolean success = launchTo(hub, cmd);
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
		int index = findEchoKey(0);
		AssertTableProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(AssertTableProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShiftAssertTable shift = getCommand();
		boolean success = (product != null);
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		// 退出
		return useful(success);
	}

}