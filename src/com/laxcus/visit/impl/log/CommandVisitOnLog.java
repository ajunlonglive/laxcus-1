/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.log;

import com.laxcus.command.*;
import com.laxcus.log.server.pool.*;
import com.laxcus.visit.*;

/**
 * LOG站点的命令调用接口
 * 
 * @author scott.liang
 * @version 1.0 10/5/2009
 * @since laxcus 1.0
 */
public class CommandVisitOnLog implements CommandVisit {

	/**
	 * 构造LOG站点的命令调用接口
	 */
	public CommandVisitOnLog() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return LogCommandPool.getInstance().accept(cmd);
	}

}