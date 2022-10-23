/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.hash;

import com.laxcus.command.*;
import com.laxcus.hash.pool.*;
import com.laxcus.visit.*;

/**
 * HASH站点的命令调用接口。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class CommandVisitOnHash implements CommandVisit {
	
	/**
	 * 构造HASH站点的命令调用接口
	 */
	public CommandVisitOnHash() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return HashCommandPool.getInstance().accept(cmd);
	}

}