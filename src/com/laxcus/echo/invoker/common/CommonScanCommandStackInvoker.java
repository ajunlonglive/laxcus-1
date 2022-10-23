/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.reload.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.site.*;

/**
 * 扫描堆栈命令调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 1/21/2019
 * @since laxcus 1.0
 */
public abstract class CommonScanCommandStackInvoker extends CommonWatchShareInvoker {

	/**
	 * 构造扫描堆栈命令调用器，指定命令
	 * @param cmd 扫描堆栈命令
	 */
	protected CommonScanCommandStackInvoker(ScanCommandStack cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ScanCommandStack getCommand() {
		return (ScanCommandStack) super.getCommand();
	}

	/**
	 * 重置扫描命令策略
	 * @return 返回扫描堆栈命令单元
	 */
	protected ScanCommandStackItem reload() {
		ScanCommandStack cmd = getCommand();
		// 重新启动任务
		boolean success = ScanEffector.reload(cmd.isStart(), cmd.getInterval());

		// 反馈给请求端
		Node local = getLocal();
		return new ScanCommandStackItem(local, success);
	}

}