/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.command.site.gate.*;
import com.laxcus.gate.pool.*;
import com.laxcus.util.*;
import com.laxcus.site.Node;

/**
 * 判断账号在GATE站点存在调用器
 * 
 * @author scott.liang
 * @version 1.0 7/18/2019
 * @since laxcus 1.0
 */
public class GateAssertGateUserInvoker extends GateInvoker {

	/**
	 * 构造判断账号在GATE站点存在调用器，指定命令
	 * @param cmd 判断账号在GATE站点存在
	 */
	public GateAssertGateUserInvoker(AssertGateUser cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AssertGateUser getCommand() {
		return (AssertGateUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AssertGateUser cmd = getCommand();
		Siger username = cmd.getUsername();

		// 判断账号存在
		boolean exists = FrontOnGatePool.getInstance().contains(username);
		// 取GATE地址，根据参数选择公网或者内网地址
		Node local = (cmd.isWide() ? getPublicListener() : getLocal());

		// 反馈结果
		AssertGateUserProduct product = new AssertGateUserProduct(exists, username, local);
		boolean success = replyProduct(product);

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