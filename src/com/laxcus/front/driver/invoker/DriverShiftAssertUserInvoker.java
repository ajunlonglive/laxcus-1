/**
 * DO NOT ShiftAssertUser OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.access.user.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 诊断用户账号存在命令调用器 <br><br>
 * 
 * 判断一个用户账号名是不是已经在集群上注册。<br>
 * 
 * @author scott.liang
 * @version 1.0 7/19/2015
 * @since laxcus 1.0
 */
public class DriverShiftAssertUserInvoker extends DriverInvoker {

	/**
	 * 构造诊断用户账号存在命令调用器 ，指定驱动任务。
	 * @param mission 驱动任务
	 */
	public DriverShiftAssertUserInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftAssertUser getCommand() {
		return (ShiftAssertUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftAssertUser shift = getCommand();
		AssertUser cmd = shift.getCommand();
		// 发送到GATE站点
		Node hub = getHub();
		boolean success = fireToHub(hub, cmd);
		// 不成功，通知退出！
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
		AssertUserProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(AssertUserProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShiftAssertUser shift = getCommand();

		boolean success = (product != null);
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		return useful(success);
	}

}
