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
 * 释放节点内存调用器
 * 
 * @author scott.liang
 * @version 1.0 10/11/2018
 * @since laxcus 1.0
 */
public abstract class CommonReleaseMemoryInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造释放节点内存调用器，指定命令
	 * @param cmd 释放节点内存
	 */
	protected CommonReleaseMemoryInvoker(ReleaseMemory cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ReleaseMemory getCommand() {
		return (ReleaseMemory) super.getCommand();
	}

	/**
	 * 释放JVM内存
	 * @return 返回真
	 */
	protected boolean clear() {
		Logger.info(this, "clear", "release memory");
		System.gc();
		return true;
	}

}