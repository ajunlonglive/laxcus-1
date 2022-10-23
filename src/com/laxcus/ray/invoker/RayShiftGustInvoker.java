/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.traffic.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 节点传输流量测试调用器。
 * 
 * @author scott.liang
 * @version 1.0 8/20/2018
 * @since laxcus 1.0
 */
public class RayShiftGustInvoker extends RayInvoker {

	/**
	 * 构造节点传输流量测试调用器，指定命令
	 * @param cmd 节点传输流量测试命令
	 */
	public RayShiftGustInvoker(ShiftGust cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftGust getCommand() {
		return (ShiftGust) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftGust shift = getCommand();
		Gust cmd = shift.getCommand();
		// 投递到目标地址
		boolean success = launchTo(cmd.getFrom(), cmd);
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
		TrafficProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TrafficProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		ShiftGust shift = getCommand();
		// 判断成功
		boolean success = (product != null);
		if (success) {
			shift.getHook().setResult(product);
		}
		shift.getHook().done();

		return useful(success);
	}

	
}
