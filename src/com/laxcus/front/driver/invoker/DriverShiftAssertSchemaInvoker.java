/**
 * DO NOT ShiftAssertSchema OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.access.schema.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 诊断数据库存在命令调用器 <br><br>
 * 
 * 判断一个数据库名是不是已经在集群上注册。<br>
 * 
 * @author scott.liang
 * @version 1.0 8/23/2015
 * @since laxcus 1.0
 */
public class DriverShiftAssertSchemaInvoker extends DriverInvoker {

	/**
	 * 构造诊断数据库存在命令调用器 ，指定驱动任务。
	 * @param mission 驱动任务
	 */
	public DriverShiftAssertSchemaInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftAssertSchema getCommand() {
		return (ShiftAssertSchema) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftAssertSchema shift = getCommand();
		AssertSchema cmd = shift.getCommand();
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
		AssertSchemaProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(AssertSchemaProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShiftAssertSchema shift = getCommand();

		boolean success = (product != null);
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		return useful(success);
	}

}
