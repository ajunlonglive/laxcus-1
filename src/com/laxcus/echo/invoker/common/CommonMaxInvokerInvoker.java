/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.mix.*;
import com.laxcus.log.client.*;
import com.laxcus.remote.client.*;

/**
 * 最大调用器数目调用器
 * 
 * @author scott.liang
 * @version 1.0 9/9/2020
 * @since laxcus 1.0
 */
public abstract class CommonMaxInvokerInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造最大调用器数目调用器，指定命令
	 * @param cmd 最大调用器数目
	 */
	protected CommonMaxInvokerInvoker(MaxInvoker cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MaxInvoker getCommand() {
		return (MaxInvoker) super.getCommand();
	}

	/**
	 * 重置参数
	 * @return 返回真
	 */
	protected boolean reset() {
		MaxInvoker cmd = getCommand();

		EchoTransfer.setMaxInvokers(cmd.getInvokers());
		EchoTransfer.setMaxConfineTime(cmd.getConfineTime() );

		Logger.info(this, "reset", "max invoker is %d, stay time:%d ms",
				EchoTransfer.getMaxInvokers(), EchoTransfer.getMaxConfineTime());

		return true;
	}

}