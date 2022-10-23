/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.rebuild.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 数据优化时间调用器
 * 
 * @author scott.liang
 * @version 1.0 11/20/2013
 * @since laxcus 1.0
 */
public class DriverRegulateTimeInvoker extends DriverInvoker {

	/**
	 * 构造数据优化时间调用器，指定驱动任务
	 * @param mission 驱动任务
	 */
	public DriverRegulateTimeInvoker(DriverMission mission) {
		super(mission);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateRegulateTime getCommand() {
		return (CreateRegulateTime) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateRegulateTime cmd = getCommand();
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
		CreateRegulateTimeProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateRegulateTimeProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			super.fault(e);
			return false;
		}

		// 判断有结果
		boolean success = (product != null);
		if (success) {
			setProduct(product);
		} else {
			CreateRegulateTime cmd = getCommand();
			faultX(FaultTip.FAILED_X, cmd.getPrimitive());
		}

		return useful(success);
	}

}