/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.front;

import com.laxcus.command.*;
import com.laxcus.front.pool.*;
import com.laxcus.visit.*;

/**
 * FRONT站点的命令调用接口
 * 
 * @author scott.liang
 * @version 1.0 10/21/2009
 * @since laxcus 1.0
 */
public class CommandVisitOnFront implements CommandVisit {

	/** FRONT命令管理池 **/
	private static FrontCommandPool instance;

	/**
	 * 设置FRONT命令管理池
	 * @param e FRONT命令管理池句柄
	 */
	public static void setCommandPool(FrontCommandPool e) {
		CommandVisitOnFront.instance = e;
	}

	/**
	 * 返回FRONT命令管理池
	 * @return FRONT命令管理池句柄
	 */
	public static FrontCommandPool getCommandPool() {
		return CommandVisitOnFront.instance;
	}

	/**
	 * 构造FRONT站点的命令调用接口
	 */
	public CommandVisitOnFront() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return instance.accept(cmd);
	}

}