/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.stub.*;
import com.laxcus.command.stub.find.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * ShiftFilteSelectStub命令调用器
 * 
 * @author scott.liang
 * @version 1.0 06/22/2013
 * @since laxcus 1.0
 */
public class CommonShiftFilteSelectStubInvoker extends CommonInvoker {

	/**
	 * 构造ShiftFilteSelectStub命令调用器，指定转发命令
	 * @param shift ShiftFilteSelectStub命令
	 */
	public CommonShiftFilteSelectStubInvoker(ShiftFilteSelectStub shift) {
		super(shift);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftFilteSelectStub getCommand() {
		return (ShiftFilteSelectStub) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftFilteSelectStub shift = getCommand();
		Node hub = shift.getHub();
		FilteSelectStub cmd = shift.getCommand();

		CommandItem item = new CommandItem(hub, cmd);
		// 向DATA站点发送命令，应答数据写入内存
		boolean success = super.completeTo(item);
		if (!success) {
			FilteSelectStubHook hook = shift.getHook();
			hook.setFault(new EchoException("send failed! to %s", hub));
		}

		Logger.debug(this, "launch", success, "send to %s", hub);

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftFilteSelectStub shift = getCommand();
		FilteSelectStubHook hook = shift.getHook();
		
		StubProduct product = null;
		try {
			if (isObjectable(0)) {
				product = getObject(StubProduct.class, 0);
			}
		} catch (VisitException e) {
			Logger.error(e);
			hook.setFault(e);
			return false;
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
