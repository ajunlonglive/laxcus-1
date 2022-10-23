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
 * 显示限制操作单元命令调用器
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopShowLimitInvoker extends DesktopLimitInvoker {

	/**
	 * 构造默认的显示限制操作单元命令调用器
	 * @param cmd 显示锁定
	 */
	public DesktopShowLimitInvoker(ShowLimit cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowLimit getCommand() {
		return (ShowLimit) super.getCommand();
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
		ShowLimitProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ShowLimitProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShowLimit cmd = getCommand();
		// 判断成功或者失败
		boolean success = (product != null);
		if (success) {
			print(product.list());
		} else {
			faultX(FaultTip.FAILED_X, cmd);
		}
		return useful(success);
	}

}