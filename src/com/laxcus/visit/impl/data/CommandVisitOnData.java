/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.data;

import com.laxcus.command.*;
import com.laxcus.visit.*;

import com.laxcus.data.pool.*;

/**
 * DATA站点的命令调用接口
 * 
 * @author scott.liang
 * @version 1.0 7/18/2009
 * @since laxcus 1.0
 */
public class CommandVisitOnData implements CommandVisit {

	/**
	 * 构造DATA站点的命令调用接口
	 */
	public CommandVisitOnData() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return DataCommandPool.getInstance().accept(cmd);
	}

}