/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.limit.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 撤销故障锁定命令调用器。<br>
 * 命令目标是GATE站点。
 * 
 * @author scott.liang
 * @version 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopDropFaultInvoker extends DesktopLimitInvoker {

	/**
	 * 构造默认的撤销故障锁定命令调用器
	 * @param cmd - 撤销锁定
	 */
	public DesktopDropFaultInvoker(DropFault cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropFault getCommand() {
		return (DropFault) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		DropFaultProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropFaultProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		DropFault cmd = getCommand();

		// 判断成功或者失败
		boolean success = (product != null);
		if (success) {
			if (product.isEmpty()) {
				warningX(WarningTip.CANNOT_IMPLEMENT_X, cmd.getPrimitive());
			} else {
				print(product.list());
			}
		} else {
			
			faultX(FaultTip.FAILED_X, cmd);
		}

		return useful(success);
	}

}