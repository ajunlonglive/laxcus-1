/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.reload.*;
import com.laxcus.log.client.*;

/**
 * 释放节点内存间隔调用器
 * 
 * @author scott.liang
 * @version 1.0 12/07/2018
 * @since laxcus 1.0
 */
public abstract class CommonReleaseMemoryIntervalInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造释放节点内存间隔调用器，指定命令
	 * @param cmd 释放节点内存
	 */
	protected CommonReleaseMemoryIntervalInvoker(ReleaseMemoryInterval cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReleaseMemoryInterval getCommand() {
		return (ReleaseMemoryInterval) super.getCommand();
	}

	/**
	 * 释放JVM内存释放间隔
	 * @return 返回真
	 */
	protected boolean clear() {
		ReleaseMemoryInterval cmd = getCommand();
		getLauncher().setReleaseMemoryInterval(cmd.getInterval());
		
		Logger.info(this, "clear", "release memory interval: %d ms",
				cmd.getInterval());

		return true;
	}

}