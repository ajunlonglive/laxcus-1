/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.data.invoker;

import com.laxcus.command.*;
import com.laxcus.data.*;
import com.laxcus.echo.invoke.*;

/**
 * DATA站点异步调用器<br>
 * 
 * 所有DATA站点上的异步调用器都从这里派生。
 * 
 * @author scott.liang
 * @version 1.1 10/12/2012
 * @since laxcus 1.0
 */
public abstract class DataInvoker extends EchoInvoker {

	/**
	 * 构造DATA站点异步调用器，指定异步命令
	 * @param cmd 异步命令
	 */
	protected DataInvoker(Command cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getLauncher()
	 */
	@Override
	public DataLauncher getLauncher() {
		return (DataLauncher) super.getLauncher();
	}

	/**
	 * 返回当前DATA站点级别
	 * @return DATA站点级别
	 */
	public byte getRank() {
		DataLauncher launcher = getLauncher();
		return launcher.getRank();
	}

	/**
	 * 判断是DATA主站点
	 * @return 返回真或者假
	 */
	public boolean isMaster() {
		DataLauncher launcher = getLauncher();
		return launcher.isMaster();
	}

	/**
	 * 判断是DATA从站点
	 * @return 返回真或者假
	 */
	public boolean isSlave() {
		DataLauncher launcher = getLauncher();
		return launcher.isSlave();
	}

}