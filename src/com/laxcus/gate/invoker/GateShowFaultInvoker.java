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
import com.laxcus.command.limit.*;
import com.laxcus.law.limit.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 显示锁定单元命令调用器。<br>
 * 系统将根据用户签名，查找它对应的拒绝操作单元。
 * 
 * @author scott.liang
 * @version 1.0 3/28/2017
 * @since laxcus 1.0
 */
public class GateShowFaultInvoker extends GateInvoker {

	/**
	 * 构造默认的显示锁定单元命令调用器
	 * @param cmd 显示锁定单元命令
	 */
	public GateShowFaultInvoker(ShowFault cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShowFault getCommand() {
		return (ShowFault) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShowFault cmd = getCommand();
		Siger issuer = cmd.getIssuer();

		// 查找已经被锁定的拒绝操作单元
		List<LimitItem> items = LimitHouse.getInstance().find(issuer);

		ShowFaultProduct product = new ShowFaultProduct();
		if (items != null) {
			product.addAll(items);
		}

		boolean success = replyProduct(product);

		Logger.debug(this, "launch", success, "limit item:%d", product.size());

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
