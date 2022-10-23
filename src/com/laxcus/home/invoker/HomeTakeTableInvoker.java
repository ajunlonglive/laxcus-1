/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.echo.*;
import com.laxcus.home.pool.*;
import com.laxcus.log.client.*;

/**
 * 查表命令调用器。<br>
 * 根据数据表名查找对应的表。
 * 
 * @author scott.liang
 * @version 1.0 2/23/2012
 * @since laxcus 1.0
 */
public class HomeTakeTableInvoker extends HomeInvoker {

	/**
	 * 构造查表命令调用器，指定命令
	 * @param cmd
	 */
	public HomeTakeTableInvoker(TakeTable cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeTable getCommand() {
		return (TakeTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeTable cmd = getCommand();

		Space space = cmd.getSpace();
		Table table = StaffOnHomePool.getInstance().findTable(space);

		boolean success = (table != null);

		if (success) {
			this.replyObject(table);
		} else {
			super.replyFault(Major.FAULTED, Minor.NOTFOUND);
		}

		Logger.debug(this, "launch", success, "take '%s'", space);

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}