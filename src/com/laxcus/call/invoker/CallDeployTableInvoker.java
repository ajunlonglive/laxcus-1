/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.call.pool.*;
import com.laxcus.site.*;

/**
 * 部署数据表调用器。
 * 
 * 部署一个数据表，同时意昧着这个资源引用的所有表，在这个节点都可以使用。
 * 
 * @author scott.liang
 * @version 1.0 6/15/2019
 * @since laxcus 1.0
 */
public class CallDeployTableInvoker extends CallInvoker {

	/**
	 * 构造部署数据表调用器，指定命令
	 * @param cmd
	 */
	public CallDeployTableInvoker(DeployTable cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DeployTable getCommand() {
		return (DeployTable) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DeployTable cmd = getCommand();
		Refer refer = cmd.getRefer();
		Table table = cmd.getTable();
		
		// 判断已经存在资源引用
		boolean success = StaffOnCallPool.getInstance().hasRefer(
				refer.getUsername());
		if (!success) {
			success = StaffOnCallPool.getInstance().create(refer);
		}
		// 建表
		if (success) {
			success = StaffOnCallPool.getInstance().createTable(refer, table);
		}
		
		// 返回结果
		Seat seat = new Seat(refer.getUsername(), getLocal());
		DeployTableItem item = new DeployTableItem(seat, success);
		DeployTableProduct product = new DeployTableProduct(item);
		success = replyProduct(product);
		
		// 延迟重新注册
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
