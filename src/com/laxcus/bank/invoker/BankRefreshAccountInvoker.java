/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import java.util.*;

import com.laxcus.bank.pool.*;
import com.laxcus.command.access.account.*;
import com.laxcus.site.*;

/**
 * 构造刷新账号调用器。<br>
 * BANK站点从ACCOUNT站点接收，转发给HASH/GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class BankRefreshAccountInvoker extends BankInvoker {

	/**
	 * 构造刷新账号调用器，指定命令
	 * @param cmd 刷新账号
	 */
	public BankRefreshAccountInvoker(RefreshAccount cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshAccount getCommand() {
		return (RefreshAccount) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshAccount cmd = getCommand();

		// 收集HASH/GATE站点
		ArrayList<Node> array = new ArrayList<Node>();
		array.addAll(HashOnBankPool.getInstance().detail());
		array.addAll(GateOnBankPool.getInstance().detail());
		// 以容错模式群发给全部HASH/GATE站点，不需要回应
		int count = directTo(array, cmd, false);
		// 退出
		return useful(count > 0);
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
