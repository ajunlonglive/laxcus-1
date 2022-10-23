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
 * 转发ASSERT INSERT命令调用器 <br>
 * 
 * 发送ASSERT INSERT命令，然后接收INSERT ASSUME命令。
 * 
 * @author scott.liang
 * @version 1.0 10/12/2013
 * @since laxcus 1.0
 */
public class CommonShiftAssertInsertInvoker extends CommonInvoker {

	/**
	 * 构造转发ASSERT INSERT命令调用器，指定转发命令
	 * @param shift ASSERT INSERT转发命令
	 */
	public CommonShiftAssertInsertInvoker(ShiftAssertInsert shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftAssertInsert getCommand() {
		return (ShiftAssertInsert)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftAssertInsert shift = getCommand();
		InsertHook hook = shift.getHook();

		Cabin hub = shift.getHub();
		ReplyItem item = new ReplyItem(hub, shift.getCommand());

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
		ShiftAssertInsert shift = getCommand();
		InsertHook hook = shift.getHook();

		int index = getEchoKeys().get(0);
		AssumeInsert assume = null;
		try {
			if (isSuccessObjectable(index)) {
				assume = getObject(AssumeInsert.class, index);
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
