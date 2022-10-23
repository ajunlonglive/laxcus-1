/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.stub.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 获取数据块编号命令调用器
 * 
 * @author scott.liang
 * @version 1.0 6/21/2009
 * @since laxcus 1.0
 */
public final class CommonShiftTakeStubInvoker extends CommonInvoker {

	/**
	 * 构造获取数据块编号命令调用器，指定命令
	 * @param cmd
	 */
	public CommonShiftTakeStubInvoker(ShiftTakeStub cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTakeStub getCommand() {
		return (ShiftTakeStub) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTakeStub shift = this.getCommand();
		TakeStub cmd = shift.getCommand();

		boolean success = completeTo(getHub(), cmd);
		if (!success) {
			TakeStubHook hook = shift.getHook();
			hook.done();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftTakeStub shift = getCommand();
		TakeStubHook hook = shift.getHook();

		StubProduct product = null;
		try {
			if (isObjectable(0)) {
				product = getObject(StubProduct.class, 0);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null);
		if (success) {
			hook.setResult(product);
		}
		hook.done();

		return useful(success);
	}

}
