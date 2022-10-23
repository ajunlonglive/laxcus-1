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

/**
 * 设置站点日志等级调用器 <br>
 * 修改本地日志等级。
 * 
 * @author scott.liang
 * @version 1.0 8/17/2017
 * @since laxcus 1.0
 */
public abstract class CommonSetLogLevelInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造设置站点日志等级调用器，指定命令
	 * @param cmd 设置站点日志等级
	 */
	protected CommonSetLogLevelInvoker(SetLogLevel cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetLogLevel getCommand() {
		return (SetLogLevel) super.getCommand();
	}
	
	/**
	 * 重新设置级别
	 */
	protected void reset() {
		SetLogLevel cmd = getCommand();
		int level = cmd.getLevel();
		Logger.setLevel(level);
	}
}