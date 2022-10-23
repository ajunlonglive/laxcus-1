/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 删除数据表调用器。<br>
 * 数据表只能由账号所有者才能删除。
 * 
 * @author scott.liang
 * @version 1.0 7/6/2018
 * @since laxcus 1.0
 */
public class AccountDropTableInvoker extends AccountInvoker {

	/**
	 * 删除数据表调用器，设置异步命令
	 * @param cmd 删除数据表
	 */
	public AccountDropTableInvoker(DropTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropTable getCommand() {
		return (DropTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropTable cmd = getCommand();
		Space space = cmd.getSpace(); // 数据表名
		Siger siger = cmd.getIssuer();

		// 删除数据表
		boolean success = StaffOnAccountPool.getInstance().dropTable(siger, space);

		// 设置状态结果
		DropTableProduct product = new DropTableProduct(space, success);

		// 成功
		if (success) {
			replyProduct(product);
		} else {
			failed();
		}

		Logger.debug(this, "launch", success, "drop %s", space);

		// 退出
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