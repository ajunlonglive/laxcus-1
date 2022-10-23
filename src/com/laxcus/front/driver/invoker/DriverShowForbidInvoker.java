/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.forbid.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 显示禁止操作单元命令调用器
 * 
 * @author scott.liang
 * @version 1.0 3/28/2017
 * @since laxcus 1.0
 */
public class DriverShowForbidInvoker extends DriverInvoker {

	/**
	 * 构造显示禁止操作单元命令调用器
	 * @param mission 驱动任务
	 */
	public DriverShowForbidInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowForbid getCommand() {
		return (ShowForbid) super.getCommand();
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
		ShowForbidProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(ShowForbidProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShowForbid cmd = getCommand();
		// 判断成功或者失败
		boolean success = (product != null);
		if (success) {
			setProduct(product);
		} else {
			faultX(FaultTip.FAILED_X, cmd);
		}
		return useful(success);
	}

}