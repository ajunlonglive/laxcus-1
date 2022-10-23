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
 * 设置用户参数调用器
 * 
 * @author scott.liang
 * @version 1.0 05/07/2017
 * @since laxcus 1.0
 */
public abstract class DriverSetMultiUserParameterInvoker extends DriverInvoker {

	/**
	 * 构造设置用户参数调用器，指定驱动任务实例
	 * @param mission 驱动任务实例
	 */
	protected DriverSetMultiUserParameterInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetMultiUserParameter getCommand() {
		return (SetMultiUserParameter) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 必须是管理员，或者等同管理员身份的用户
		boolean success = isAdministrator();
		if (!success) {
			success = getStaffPool().canDBA();
		}
		if (!success) {
			faultX(FaultTip.PERMISSION_MISSING);
			return false;
		}
		// 发送命令
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		SetMultiUserParameterProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SetMultiUserParameterProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			setProduct(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		// 退出
		return useful(success);
	}
}