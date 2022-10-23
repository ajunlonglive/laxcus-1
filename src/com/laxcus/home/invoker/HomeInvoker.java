/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.*;
import com.laxcus.echo.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.home.*;

/**
 * HOME站点异步调用器。<br>
 * 
 * 在HOME站点上运行的异步调用器都从这里派生。
 * 
 * @author scott.liang
 * @version 1.1 05/06/2013
 * @since laxcus 1.0
 */
public abstract class HomeInvoker extends EchoInvoker {

	/**
	 * 构造HOME站点异步调用器，指定异步命令
	 * @param cmd 异步命令
	 */
	protected HomeInvoker(Command cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public HomeLauncher getLauncher() {
		return (HomeLauncher) super.getLauncher();
	}

	/**
	 * 判断是管理站点
	 * @return 返回真或者假
	 */
	public boolean isManager() {
		HomeLauncher launcher = getLauncher();
		return launcher.isManager();
	}

	/**
	 * 判断是监视站点
	 * @return 返回真或者假
	 */
	public boolean isMonitor() {
		HomeLauncher launcher = getLauncher();
		return launcher.isMonitor();
	}

	/**
	 * 反馈一个拒绝操作
	 * @return 发送成功返回真，否则假
	 */
	protected boolean refuse() {
		return replyFault(Major.FAULTED, Minor.REFUSE);
	}

}
