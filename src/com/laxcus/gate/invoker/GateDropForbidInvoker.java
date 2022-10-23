/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.gate.pool.*;
import com.laxcus.command.forbid.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 撤销禁止操作单元命令调用器。<br>
 * 
 * 这个命令执行后，相关的数据资源被解除禁止操作，恢复读写操作。
 * 
 * @author scott.liang
 * @version 1.0 4/1/2017
 * @since laxcus 1.0
 */
public class GateDropForbidInvoker extends GateInvoker {

	/**
	 * 构造撤销禁止操作单元命令调用器，指定命令。
	 * @param cmd 撤销禁止操作单元命令
	 */
	public GateDropForbidInvoker(DropForbid cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropForbid getCommand() {
		return (DropForbid) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropForbid cmd = getCommand();
		Siger siger = cmd.getIssuer();

		// 撤销全部禁止操作的资源
		boolean success = ForbidHouse.getInstance().revoke(siger, cmd.list());

		Logger.debug(this, "launch", success, "Drop forbid, To %s",
				getCommandSource());

		// 撤销成功，返回被撤销的禁止操作单元；否则返回错误码
		if (success) {
			DropForbidProduct product = new DropForbidProduct();
			product.addAll(cmd.list());
			success = replyProduct(product);
		} else {
			// 返回错误提示
			replyFault(Major.FAULTED, Minor.GRANT_ERROR);
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