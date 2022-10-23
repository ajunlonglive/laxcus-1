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
 * 建立限制操作命令调用器
 * 
 * @author scott.liang
 * @version 3/23/2017
 * @since laxcus 1.0
 */
public class DesktopCreateLimitInvoker extends DesktopLimitInvoker {

	/**
	 * 构造默认的建立限制操作命令调用器
	 * @param cmd
	 */
	public DesktopCreateLimitInvoker(CreateLimit cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateLimit getCommand() {
		return (CreateLimit) super.getCommand();
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
		CreateLimitProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateLimitProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		CreateLimit cmd = getCommand();

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