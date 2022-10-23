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
 * 提交禁止操作命令调用器。<br>
 * 
 * 命令生效后，相关的资源被全部锁定，不允许做任何读写操作，直到被撤销。
 * 
 * @author scott.liang
 * @version 1.0 4/1/2017
 * @since laxcus 1.0
 */
public class GateCreateForbidInvoker extends GateInvoker {

	/**
	 * 构造提交禁止操作命令调用器，指定命令
	 * @param cmd 提交禁止操作命令
	 */
	public GateCreateForbidInvoker(CreateForbid cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateForbid getCommand() {
		return (CreateForbid) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateForbid cmd = getCommand();
		Siger siger = cmd.getIssuer();

		// 1. 判断与正在运行的事务规则存在冲突
		boolean conflit = RuleHouse.getInstance().conflict(siger, cmd.list());
		if (conflit) {
			Logger.warning(this, "launch", "rule conflict! from: %s", siger);

			// 向请求端返回错误提示
			replyFault(Major.FAULTED, Minor.CANNOT_CREATE_FORBID);
			return useful(false);
		}

		// 2. 提交到管理池
		boolean success = ForbidHouse.getInstance().submit(siger, cmd.list());
		// 提交失败（存在冲突），返回错误
		if (!success) {
			Logger.warning(this, "launch", "cannot be submit forbid! from: %s", siger);

			replyFault(Major.FAULTED, Minor.CANNOT_CREATE_FORBID);
			return useful(false);
		}

		// 返回应答报告
		CreateForbidProduct product = new CreateForbidProduct();
		product.addAll(cmd.list());
		success = replyProduct(product);

		// 操作结果
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