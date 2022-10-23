/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.access.account.*;
import com.laxcus.log.client.*;
import com.laxcus.top.pool.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 获取账号命令调任器<br>
 * 
 * 命令来自HOME或者它的子站点。
 * 
 * @author scott.liang
 * @version 1.0 5/23/2009
 * @since laxcus 1.0
 */
public class TopTakeAccountInvoker extends TopInvoker {

	/**
	 * 构造获取账号命令调任器，指定命令
	 * @param cmd 获取账号命令
	 */
	public TopTakeAccountInvoker(TakeAccount cmd) {
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
		Node slave = BankOnTopPool.getInstance().getManagerSite();
		boolean success = (slave != null);
		if (success) {
			TakeAccount cmd = getCommand();
			success = launchTo(slave, cmd);
		}
		if (!success) {
			failed();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 判断处理结果
		TakeAccountProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeAccountProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null && product.getAccount() != null);
		if (success) {
			success = replyProduct(product);
		}
		if (!success) {
			notfound();
		}
		return useful(success);
	}

}