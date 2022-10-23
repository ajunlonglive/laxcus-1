/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.watch;

import com.laxcus.command.*;
import com.laxcus.echo.invoke.*;
import com.laxcus.visit.*;

/**
 * WATCH站点的命令调用接口
 * 
 * 包括来自WATCH节点或者RAY节点
 * 
 * @author scott.liang
 * @version 1.0 11/08/2012
 * @since laxcus 1.0
 */
public class CommandVisitOnWatch implements CommandVisit {

	/** 命令管理池 **/
	private static CommandPool instance;

	/**
	 * 设置命令管理池
	 * @param e 命令管理池句柄
	 */
	public static void setCommandPool(CommandPool e) {
		CommandVisitOnWatch.instance = e;
	}

	/**
	 * 返回命令管理池
	 * @return 命令管理池句柄
	 */
	public static CommandPool getCommandPool() {
		return CommandVisitOnWatch.instance;
	}

	/**
	 * 构造WATCH站点的命令调用接口
	 */
	public CommandVisitOnWatch() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return CommandVisitOnWatch.instance.accept(cmd);
		// return WatchCommandPool.getInstance().accept(cmd);
	}

}