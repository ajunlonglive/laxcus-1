/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.account;import com.laxcus.command.*;
import com.laxcus.account.pool.*;
import com.laxcus.visit.*;

/**
 * ACCOUNT站点的命令调用接口。
 * 
 * @author scott.liang
 * @version 1.0 6/21/2018
 * @since laxcus 1.0
 */
public class CommandVisitOnAccount implements CommandVisit {
	
	/**
	 * 构造ACCOUNT站点的命令调用接口
	 */
	public CommandVisitOnAccount() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return AccountCommandPool.getInstance().accept(cmd);
	}

}