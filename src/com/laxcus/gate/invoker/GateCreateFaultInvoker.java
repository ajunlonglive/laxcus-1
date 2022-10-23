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
import com.laxcus.echo.*;
import com.laxcus.law.limit.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 故障锁定命令调用器。<br>
 * 命令从FRONT站点，GATE站点接收后，锁定单元单元保存在本地内存中。
 * 
 * @author scott.liang
 * @version 1.0 3/24/2017
 * @since laxcus 1.0
 */
public class GateCreateFaultInvoker extends GateInvoker {

	/**
	 * 构造故障锁定命令调用器，指定命令
	 * @param cmd 故障锁定命令命令
	 */
	public GateCreateFaultInvoker(CreateFault cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CreateFault getCommand() {
		return (CreateFault) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		CreateFault cmd = getCommand();
		Siger issuer = cmd.getIssuer();

		// 判断账号存在
		boolean success = StaffOnGatePool.getInstance().contains(issuer);
		if (!success) {
			if (cmd.isReply()) replyFault(Major.FAULTED, Minor.REFUSE);
			return useful(false);
		}

		// 根据故障锁定单元，提取关联的限制操作单元
		List<LimitItem> limits = StaffOnGatePool.getInstance().dress(issuer, cmd.list());

		// 故障锁定命令结果
		CreateFaultProduct product = new CreateFaultProduct();
		if (limits.size() > 0) {
			List<LimitItem> results = LimitHouse.getInstance().submit(issuer, limits);
			product.addAll(results);
		}

		// 如果命令要求返回应答，把处理结果返回给请求端
		if (cmd.isReply()) {
			success = replyProduct(product);
		}

		Logger.debug(this, "launch", success, "limit item:%d", product.size());

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