/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.access.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.echo.*;
import com.laxcus.visit.*;

/**
 * 转发ASSERT DELETE命令调用器 <br>
 * 
 * 发送ASSERT DELETE命令，然后接收DELETE ASSUME命令。
 * 
 * @author scott.liang
 * @version 1.0 10/12/2013
 * @since laxcus 1.0
 */
public class CommonShiftAssertDeleteInvoker extends CommonInvoker {

	/**
	 * 构造转发ASSERT DELETE命令调用器，指定转发命令
	 * @param shift ASSERT DELETE转发命令
	 */
	public CommonShiftAssertDeleteInvoker(ShiftAssertDelete shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftAssertDelete getCommand() {
		return (ShiftAssertDelete)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftAssertDelete shift = getCommand();
		DeleteHook hook = shift.getHook();

		Cabin hub = shift.getHub();
		ReplyItem item = new ReplyItem(hub, shift.getCommand());

		// 投递到目标站点
		boolean success = replyTo(item);
		if (!success) {
			hook.setFault(new EchoException("cannot be send to %s", hub));
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftAssertDelete shift = getCommand();
		DeleteHook hook = shift.getHook();

		int index = getEchoKeys().get(0);
		AssumeDelete assume = null;
		try {
			if (isSuccessObjectable(index)) {
				assume = getObject(AssumeDelete.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
			hook.setFault(e);
			return false;
		}
		// 以上不成功，退出
		boolean success = (assume != null);

		// 成功或者失败
		if (success) {
			hook.setResult(assume);
		} else {
			hook.setFault(new EchoException("illegal assume insert"));
		}

		// 完成
		return useful(success);
	}

}
