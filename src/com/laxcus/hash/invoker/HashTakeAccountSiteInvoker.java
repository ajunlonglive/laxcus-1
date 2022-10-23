/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.invoker;

import com.laxcus.command.site.bank.*;
import com.laxcus.hash.pool.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.util.*;

/**
 * 查询账号所在的ACCOUNT站点调用器。<br>
 * GATE/BANK发出，HASH接收。
 * 
 * @author scott.liang
 * @version 1.0 6/29/2018
 * @since laxcus 1.0
 */
public class HashTakeAccountSiteInvoker extends HashInvoker {

	/**
	 * 构造查询账号所在的ACCOUNT站点，指定命令
	 * @param cmd 查询账号所在的ACCOUNT站点命令
	 */
	public HashTakeAccountSiteInvoker(TakeAccountSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeAccountSite getCommand() {
		return (TakeAccountSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeAccountSite cmd = getCommand();
		Siger siger = cmd.getSiger();
		// 查询ACCOUNT站点
		Node remote = StaffOnHashPool.getInstance().findAccountSite(siger);

		// 判断成功
		boolean success = (remote != null);
		// 反馈结果
		TakeAccountSiteProduct product = new TakeAccountSiteProduct(siger, remote);

		if (!success) {
			Logger.error(this, "launch", "cannot be find %s ", siger);
		} else {
			//	Logger.info(this, "launch", "check %s to %s", siger, remote);
		}
		
		// 反馈给GATE站点
		replyProduct(product);
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
