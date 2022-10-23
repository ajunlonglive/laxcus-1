/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.access.diagram.*;
import com.laxcus.command.access.user.*;
import com.laxcus.bank.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 授权修改账号密码调用器 <br>
 * 
 * BANK将命令转发给GATE/TOP站点。
 * 
 * @author scott.liang
 * @version 1.0 7/5/2018
 * @since laxcus 1.0
 */
public class BankAwardAlterUserInvoker extends BankInvoker {

	/**
	 * 构造授权修改账号密码调用器，指定命令
	 * @param cmd 授权修改账号密码命令
	 */
	public BankAwardAlterUserInvoker(AwardAlterUser cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public AwardAlterUser getCommand() {
		return (AwardAlterUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		AwardAlterUser cmd = getCommand();
		User user = cmd.getUser();

		// 取出GATE/TOP站点
		ArrayList<Node> array = new ArrayList<Node>();
		array.addAll(GateOnBankPool.getInstance().detail());
		array.add(getHub());

		// 投递给GATE/TOP站点
		int count = directTo(array, cmd);
		boolean success = (count > 0);

		Logger.debug(this, "launch", success, "alter %s", user);

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