/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import java.util.*;

import com.laxcus.gate.pool.*;
import com.laxcus.command.forbid.*;
import com.laxcus.law.forbid.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 显示禁止操作调用器
 * 
 * @author scott.liang
 * @version 1.0 4/1/2017
 * @since laxcus 1.0
 */
public class GateShowForbidInvoker extends GateInvoker {

	/**
	 * 构造显示禁止操作调用器，指定命令
	 * @param cmd 显示禁止操作
	 */
	public GateShowForbidInvoker(ShowForbid cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowForbid getCommand() {
		return (ShowForbid) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShowForbid cmd = getCommand();
		Siger issuer = cmd.getIssuer();

		// 查找
		List<ForbidItem> array = ForbidHouse.getInstance().find(issuer);
		ShowForbidProduct product = new ShowForbidProduct();
		if (array != null) {
			product.addAll(array);
		}

		// 返回处理结果给请求端
		boolean success = replyProduct(product);
		Logger.debug(this, "launch", success, "forbid item size is:%d",
				product.size());
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