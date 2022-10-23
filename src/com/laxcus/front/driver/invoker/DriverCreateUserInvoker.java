/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 账号命令调用器
 * 
 * @author scott.liang
 * @version 1.1 2/23/2014
 * @since laxcus 1.0
 */
public class DriverCreateUserInvoker extends DriverInvoker {

	/**
	 * 构造账号命令调用器，指定连接器。
	 * @param mission 驱动任务
	 */
	public DriverCreateUserInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateUser getCommand() {
		return (CreateUser) super.getCommand();
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
		CreateUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(CreateUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		// 判断成功
		boolean success = (product != null);
		
		if (success) {
			setProduct(product);
		} else {
			CreateUser cmd = getCommand();
			faultX(FaultTip.FAILED_X, cmd);
		}

		return useful(success);
	}

}