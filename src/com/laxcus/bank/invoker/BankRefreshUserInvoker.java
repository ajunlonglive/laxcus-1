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
import com.laxcus.command.access.user.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;
import com.laxcus.site.*;

/**
 * 刷新注册用户命令调用器。
 * 
 * BANK站点负责将命令转发给GATE站点，由GATE站点进行分派，并将处理结果转发给BANK站点。
 * 
 * @author scott.liang
 * @version 1.0 7/2/2018
 * @since laxcus 1.0
 */
public class BankRefreshUserInvoker extends BankInvoker {

	/**
	 * 刷新注册用户命令调用器，指定命令
	 * @param cmd 刷新注册用户命令
	 */
	public BankRefreshUserInvoker(RefreshUser cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public RefreshUser getCommand() {
		return (RefreshUser) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		RefreshUser cmd = getCommand();

		// 取全部GATE站点
		List<Node> slaves = GateOnBankPool.getInstance().detail();
		// 发容错模式，发送到全部GATE站点
		int count = incompleteTo(slaves, cmd);
		// 判断成功
		boolean success = (count > 0);
		// 不成功，通知WATCH站点错误
		if (!success) {
			replyFault();
		}
		// 释放
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		RefreshUserProduct product = new RefreshUserProduct();
		List<Integer> keys = super.getEchoKeys();
		for (int index : keys) {
			try {
				if (isSuccessObjectable(index)) {
					RefreshUserProduct e = getObject(RefreshUserProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}
		// 反馈结果
		boolean success = replyProduct(product);
		return useful(success);
	}

}
