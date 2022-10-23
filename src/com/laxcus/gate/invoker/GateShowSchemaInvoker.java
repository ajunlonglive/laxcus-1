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
import com.laxcus.command.access.schema.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 显示数据库命令调用器 
 * 
 * @author scott.liang
 * @version 1.0 4/23/2014
 * @since laxcus 1.0
 */
public class GateShowSchemaInvoker extends GateInvoker {

	/**
	 * 构造显示数据库命令调用器，指定命令
	 * @param cmd 显示数据库命令
	 */
	public GateShowSchemaInvoker(ShowSchema cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowSchema getCommand() {
		return (ShowSchema) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShowSchema cmd = getCommand();
		Siger siger = cmd.getIssuer();

		// 查找账号
		Account account = StaffOnGatePool.getInstance().findAccount(siger, true);
		// 没有找到，返回错误
		if (account == null) {
			replyFault(Major.FAULTED, Minor.NOTFOUND);
			return false;
		}

		ArrayList<Fame> array = new ArrayList<Fame>();
		// 提取全部数据库配置
		if (cmd.isAll()) {
			array.addAll(account.getFames());
		} else {
			array.addAll(cmd.list());
		}

		// 取出关联的数据库
		SchemaProduct product = new SchemaProduct();
		for (Fame fame : array) {
			Schema schema = account.findSchema(fame);
			if (schema != null) product.add(schema);
		}

		// 返回检查结果
		boolean success = replyProduct(product);
		Logger.debug(this, "launch", success, "schema size:%d", product.size());
		// 退出
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