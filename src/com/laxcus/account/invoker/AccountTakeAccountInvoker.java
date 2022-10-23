/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import com.laxcus.access.diagram.*;
import com.laxcus.account.dict.*;
import com.laxcus.command.access.account.*;
import com.laxcus.util.*;

/**
 * 获取账号调用器。<br>
 * GATE发出，ACCOUNT接收。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class AccountTakeAccountInvoker extends AccountInvoker {

	/**
	 * 构造获取账号，指定命令
	 * @param cmd 获取账号命令
	 */
	public AccountTakeAccountInvoker(TakeAccount cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeAccount getCommand() {
		return (TakeAccount) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeAccount cmd = getCommand();
		Siger siger = cmd.getSiger();
		// 获取账号配置
		Account account = StaffOnAccountPool.getInstance().readAccount(siger);
		boolean success = (account != null);
		// 反馈结果
		TakeAccountProduct product = new TakeAccountProduct(account);
		success = replyProduct(product);
		
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