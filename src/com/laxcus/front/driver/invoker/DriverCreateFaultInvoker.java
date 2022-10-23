/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.limit.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 提交故障锁定命令调用器。<br>
 * 锁定操作目标是AID站点。
 * 
 * @author scott.liang
 * @version 1.0 3/27/2017
 * @since laxcus 1.0
 */
public class DriverCreateFaultInvoker extends DriverInvoker {

	/**
	 * 构造提交故障锁定命令调用器
	 * @param mission 驱动任务
	 */
	public DriverCreateFaultInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateFault getCommand() {
		return (CreateFault) super.getCommand();
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
		CreateFaultProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateFaultProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		CreateFault cmd = getCommand();

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