/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.top;

import com.laxcus.command.*;
import com.laxcus.top.pool.*;
import com.laxcus.visit.*;

/**
 * TOP站点的命令调用接口
 * 
 * @author scott.liang
 * @version 1.0 7/12/2009
 * @since laxcus 1.0
 */
public class CommandVisitOnTop implements CommandVisit {

	/**
	 * 构造TOP站点的命令调用接口
	 */
	public CommandVisitOnTop() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return TopCommandPool.getInstance().accept(cmd);
	}

}