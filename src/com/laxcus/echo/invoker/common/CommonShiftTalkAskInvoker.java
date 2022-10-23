/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.task.talk.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 分布任务组件之间交互调用器
 * 
 * @author scott.liang
 * @version 1.0 6/14/2018
 * @since laxcus 1.0
 */
public final class CommonShiftTalkAskInvoker extends CommonInvoker {

	/**
	 * 构造分布任务组件之间交互调用器，指定转发命令
	 * @param shift 转发分布任务组件之间交互命令
	 */
	public CommonShiftTalkAskInvoker(ShiftTalkAsk shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftTalkAsk getCommand() {
		return (ShiftTalkAsk) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftTalkAsk shift = getCommand();
		TalkAsk cmd = shift.getCommand();
		Node hub = shift.getRemote();

		// 投递到目标站点
		boolean success = completeTo(hub, cmd);
		if (!success) {
			TalkAskHook hook = shift.getHook();
			hook.done();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShiftTalkAsk shift = getCommand();
		TalkAskHook hook = shift.getHook();

		int index = findEchoKey(0);
		TalkAskProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TalkAskProduct.class, index);
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
