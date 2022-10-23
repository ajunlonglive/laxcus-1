/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.access.fast.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 修改数据块尺寸调用器
 * 
 * @author scott.liang
 * @version 1.0 10/01/2016
 * @since laxcus 1.0
 */
public class DriverSetEntitySizeInvoker extends DriverInvoker {

	/**
	 * 构造修改数据块尺寸调用器，指定驱动任务
	 * @param mission 驱动任务
	 */
	public DriverSetEntitySizeInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetEntitySize getCommand() {
		return (SetEntitySize) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SetEntitySize cmd = getCommand();
		Node hub = getHub();

		boolean success = completeTo(hub, cmd);
		if (!success) {
			super.faultX(FaultTip.CANNOT_SUBMIT_X, hub);
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		SetEntitySizeProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SetEntitySizeProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			setProduct(product);
		} else {
			SetEntitySize cmd = getCommand();
			faultX(FaultTip.FAILED_X, cmd.getPrimitive());
		}

		return useful(success);
	}

}
