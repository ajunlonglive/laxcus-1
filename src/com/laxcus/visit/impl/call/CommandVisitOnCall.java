/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.call;

import com.laxcus.command.*;
import com.laxcus.call.pool.*;
import com.laxcus.visit.*;

/**
 * CALL站点的命令调用接口
 * 
 * @author scott.liang
 * @version 1.0 07/19/2009
 * @since laxcus 1.0
 */
public class CommandVisitOnCall implements CommandVisit {

	/**
	 * 构造CALL站点的命令调用接口
	 */
	public CommandVisitOnCall() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return CallCommandPool.getInstance().accept(cmd);
	}

}