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
import com.laxcus.call.pool.*;
import com.laxcus.command.access.table.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.site.call.*;

/**
 * 授权建表命令调用器。
 * 建表命令由HOME站点发送，要求CALL建立一个表资源。CALL站点在收到命令后，检索与这个表配置相关的阶段命名，向ACCOUNT站点请求部署。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2013
 * @since laxcus
 */
public class CallAwardCreateTableInvoker extends CallInvoker {

	/**
	 * 构造授权建表命令调用器，指定命令
	 * @param cmd 授权建表命令
	 */
	public CallAwardCreateTableInvoker(AwardCreateTable cmd) {
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
	 * 向BANK站点反馈结果
	 * @param success
	 */
	private boolean reply(boolean success) {
		Space space = getCommand().getTable().getSpace();
		CreateTableProduct product = new CreateTableProduct(space, success);
		if (success) {
			CallSite site = getLauncher().getSite();
			// 同时提供内网和公网地址，GATE节点判断后重新整理输出给FRONT节点！
			GatewayNode gateway = new GatewayNode(site.getPrivate(), site.getPublic());
			product.add(gateway);
			// product.add(getPublicListener());
		}
		return replyProduct(product);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardCreateTable cmd = getCommand();
		Refer refer = cmd.getRefer();
		Table table = cmd.getTable();
		boolean success = StaffOnCallPool.getInstance().createTable(refer, table);
		// 保存成功，通知重新注册
		if (success) {
			getLauncher().checkin(false);
		}

		// 发送处理结果
		success = reply(success);
		
		if (!success) {
			StaffOnCallPool.getInstance().dropTable(table.getSpace());
		}

		Logger.debug(this, "launch", success, "create '%s'", table.getSpace());
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
