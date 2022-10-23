/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.build.invoker;

import com.laxcus.command.refer.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 转发请求资源引用命令调用器
 * 
 * @author scott.liang
 * @version 1.0 7/23/2012
 * @since laxcus 1.0
 */
public class BuildShiftRequestReferInvoker extends BuildInvoker {

	/**
	 * 构造转发请求资源引用命令调用器，指定命令
	 * @param shift 转发命令
	 */
	public BuildShiftRequestReferInvoker(ShiftRequestRefer shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftRequestRefer getCommand() {
		return (ShiftRequestRefer) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftRequestRefer shift = getCommand();

		RequestRefer cmd = shift.getCommand();
		boolean success = launchToHub(cmd);
		// 不成功，唤醒事件钩子
		if (!success) {
			RequestReferHook hook = shift.getHook();
			hook.done();
		}
		
		Logger.debug(this, "launch", success, "result is");

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftRequestRefer shift = getCommand();
		RequestReferHook hook = shift.getHook();
		int index = findEchoKey(0);

		RequestReferProduct product = null;
		try {
			if (isObjectable(index)) {
				product = getObject(RequestReferProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		}
		// 用事件钩子唤醒等待的方法
		hook.done();

		Logger.debug(this, "ending", success, "result is");

		return useful(success);
	}

}