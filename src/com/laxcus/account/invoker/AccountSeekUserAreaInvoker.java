/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.account.invoker;

import java.util.*;

import com.laxcus.command.access.user.*;
import com.laxcus.account.dict.*;
import com.laxcus.log.client.*;
import com.laxcus.util.*;

/**
 * 检索用户分布区域调用器。<br>
 * 
 * @author scott.liang
 * @version 1.0 5/29/2019
 * @since laxcus 1.0
 */
public class AccountSeekUserAreaInvoker extends AccountInvoker {

	/**
	 * 构造检索用户分布区域调用器，指定命令
	 * @param cmd 检索用户分布区域命令
	 */
	public AccountSeekUserAreaInvoker(SeekUserArea cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SeekUserArea getCommand() {
		return (SeekUserArea) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SeekUserArea cmd = getCommand();
		List<Siger> sigers = cmd.getUsers();

		SeekUserAreaProduct product = new SeekUserAreaProduct();
		// 检索相关用户存在，如果是，保存它！
		for (Siger siger : sigers) {
			boolean success = StaffOnAccountPool.getInstance().hasAccount(siger);
			if (success) {
				product.add(siger, getLocal());
			}
		}
		// 返回结果
		replyProduct(product);

		Logger.debug(this, "launch", "siger count:%d", product.size());

		return useful();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		return false;
	}
	
}