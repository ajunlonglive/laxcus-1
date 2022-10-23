/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import com.laxcus.command.stub.find.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * ShiftFindSpacePrimeSite命令调用器
 * 
 * @author scott.liang
 * @version 1.0 06/22/2013
 * @since laxcus 1.0
 */
public class WorkShiftFindSpacePrimeSiteInvoker extends WorkInvoker {

	/**
	 * 构造ShiftFindSpacePrimeSite命令调用器，指定转发命令
	 * @param cmd - ShiftFindSpacePrimeSite命令
	 */
	public WorkShiftFindSpacePrimeSiteInvoker(ShiftFindSpacePrimeSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftFindSpacePrimeSite getCommand() {
		return (ShiftFindSpacePrimeSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftFindSpacePrimeSite shift = getCommand();
		Node hub = shift.getHub();
		FindSpacePrimeSite cmd = shift.getCommand();

		CommandItem item = new CommandItem(hub, cmd);
		// 发送给CALL站点，应答数据写入内存
		boolean success = completeTo(item);
		if (!success) {
			FindSpacePrimeSiteHook hook = shift.getHook();
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
		ShiftFindSpacePrimeSite shift = getCommand();
		FindSpacePrimeSiteHook hook = shift.getHook();
		
		FindSpacePrimeSiteProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(FindSpacePrimeSiteProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			hook.setFault(e);
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
