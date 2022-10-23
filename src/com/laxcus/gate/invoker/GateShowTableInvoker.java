/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.access.schema.*;
import com.laxcus.gate.pool.*;
import com.laxcus.command.access.table.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 显示数据表命令调用器
 * 
 * @author scott.liang
 * @version 1.0 4/23/2013
 * @since laxcus 1.0
 */
public class GateShowTableInvoker extends GateInvoker {

	/**
	 * 构造显示数据表命令调用器，指定命令
	 * @param cmd 数据表命令
	 */
	public GateShowTableInvoker(ShowTable cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowTable getCommand() {
		return (ShowTable) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShowTable cmd = getCommand();
		Siger siger = cmd.getIssuer();

		// 查找账号
		Account account = StaffOnGatePool.getInstance().findAccount(siger, true);
		// 没有找到，返回错误
		if (account == null) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
			return false;
		}

		ArrayList<Space> array = new ArrayList<Space>();
		// 提取全部数据库配置
		if (cmd.isAll()) {
			array.addAll(account.getSpaces());
		} else {
			array.addAll(cmd.list());
		}

		// 查找数据表
		TableProduct product = new TableProduct();
		for (Space space : array) {
			Table table = account.findTable(space);
			if (table != null) product.add(table);
		}

		// 返回检查结果
		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "table size:%d", product.size());

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
}