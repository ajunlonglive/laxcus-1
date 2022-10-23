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
import com.laxcus.command.site.entrance.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;
import com.laxcus.visit.*;

/**
 * 定位GATE站点模式调用器
 * 
 * @author scott.liang
 * @version 1.0 7/19/2019
 * @since laxcus 1.0
 */
public class BankShadowModeInvoker extends BankInvoker {

	/**
	 * 构造定位GATE站点模式调用器，指定命令
	 * @param cmd 定位GATE站点模式
	 */
	public BankShadowModeInvoker(ShadowMode cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShadowMode getCommand() {
		return (ShadowMode) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		List<Node> slaves = EntranceOnBankPool.getInstance().detail();
		ShadowMode cmd = getCommand();
		int count = incompleteTo(slaves, cmd);
		boolean success = (count > 0);
		if (!success) {
			replyProduct(new ShadowModeProduct());
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		ShadowModeProduct product = new ShadowModeProduct();
		List<Integer> keys = getEchoKeys();
		for (int index : keys) {
			try {
				if (this.isSuccessCompleted(index)) {
					ShadowModeProduct e = getObject(ShadowModeProduct.class, index);
					product.addAll(e);
				}
			} catch (VisitException e) {
				Logger.error(e);
			}
		}

		// 反馈结果
		replyProduct(product);

		return useful();
	}

}
