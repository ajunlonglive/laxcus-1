/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.visit.impl.bank;

import com.laxcus.command.*;
import com.laxcus.bank.pool.*;
import com.laxcus.visit.*;

/**
 * BANK站点的命令调用接口。<br>
 * 做一个事：接收异步命令，投递给BANK命令管理池。
 * 
 * @author scott.liang
 * @version 1.0 6/25/2018
 * @since laxcus 1.0
 */
public class CommandVisitOnBank implements CommandVisit {

	/**
	 * 构造BANK站点的命令调用接口
	 */
	public CommandVisitOnBank() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.visit.CommandVisit#submit(com.laxcus.command.Command)
	 */
	@Override
	public boolean submit(Command cmd) throws VisitException {
		return BankCommandPool.getInstance().accept(cmd);
	}

}