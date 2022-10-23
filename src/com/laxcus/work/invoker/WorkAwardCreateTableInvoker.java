/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.work.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;
import com.laxcus.work.pool.*;

/**
 * 授权建表调用器。<br>
 * HOME授权建立一个数据表，WORK站点无条件接受并且执行。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus 1.0
 */
public class WorkAwardCreateTableInvoker extends WorkInvoker {

	/**
	 * 构造授权建表调用器，指定命令
	 * @param cmd 授权建表命令
	 */
	public WorkAwardCreateTableInvoker(AwardCreateTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardCreateTable getCommand() {
		return (AwardCreateTable) super.getCommand();
	}

	/**
	 * 向HOME站点反馈结果
	 * @param success
	 */
	private boolean reply(boolean success) {
		Space space = getCommand().getTable().getSpace();
		CreateTableProduct product = new CreateTableProduct(space, success);
		product.setSuccessful(success);
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardCreateTable cmd = getCommand();
		Table table = cmd.getTable();
		Siger siger = table.getIssuer();

		// 查询有账号
		boolean exists = StaffOnWorkPool.getInstance().allow(siger);
		// 建表
		boolean success = StaffOnWorkPool.getInstance().createTable(table);

		// 账号不存在时，要加载分布组件
		if (!exists && success) {
			StaffOnWorkPool.getInstance().loadTasks(siger);
		}

		Logger.debug(this, "launch", success, "create '%s'", table);

		// 向HOME发送应答
		success = reply(success);
		
		// 延时触发
		if (success) {
			getLauncher().checkin(false);
		}

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
