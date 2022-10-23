/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.gate.invoker;

import com.laxcus.gate.*;
import com.laxcus.command.cyber.*;
import com.laxcus.util.cyber.*;

/**
 * 检测集群用户虚拟空间调用器
 * 
 * @author scott.liang
 * @version 1.0 10/28/2019
 * @since laxcus 1.0
 */
public class GateCheckUserCyberInvoker extends GateInvoker {

	/**
	 * 构造检测集群用户虚拟空间调用器，指定命令
	 * @param cmd 检测集群用户虚拟空间
	 */
	public GateCheckUserCyberInvoker(CheckUserCyber cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public CheckUserCyber getCommand(){
		return (CheckUserCyber)super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		GateLauncher launcher = getLauncher();
		Moment moment = launcher.createMoment();

		UserCyberItem item = new UserCyberItem(getLocal(), moment);
		UserCyberProduct product = new UserCyberProduct(item);
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
