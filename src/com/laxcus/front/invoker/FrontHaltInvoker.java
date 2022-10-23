/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.invoker;

import com.laxcus.command.halt.*;
import com.laxcus.echo.*;
import com.laxcus.front.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 中断命令调用器。<br>
 * 
 * 中断命令从GATE站点发来。判断地址来源，关闭与服务器的连接。按照协议规定，不需要反馈应答。
 * 
 * @author scott.liang
 * @version 1.0 7/12/2012
 * @since laxcus 1.0
 */
public class FrontHaltInvoker extends FrontInvoker {

	/**
	 * 构造中断命令调用器，指定命令
	 * @param cmd 服务器中断命令
	 */
	public FrontHaltInvoker(Halt cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public Halt getCommand() {
		return (Halt) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		Halt cmd = getCommand();

		Cabin cabin = cmd.getSource();
		Node hub = cabin.getNode();

		// 注销连接
		boolean success = CallOnFrontPool.getInstance().logout(hub);

		Logger.debug(this, "launch", success, "logout from %s", hub);

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
