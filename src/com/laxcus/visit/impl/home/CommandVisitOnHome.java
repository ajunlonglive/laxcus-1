/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.home;

import com.laxcus.command.*;
import com.laxcus.home.pool.*;
import com.laxcus.visit.*;

/**
 * HOME站点的命令调用接口
 * 
 * @author scott.liang
 * @version 1.0 07/19/2009
 * @since laxcus 1.0
 */
public class CommandVisitOnHome implements CommandVisit {

	/**
	 * 构造HOME站点的命令调用接口
	 */
	public CommandVisitOnHome() {
		super();
	}

	/**
	 * 接受和转发异步命令
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return HomeCommandPool.getInstance().accept(cmd);
	}

}