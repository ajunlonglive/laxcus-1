/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.relate.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * 获取CALL站点成员调用器。<br>
 * 
 * BANK在此是中继站点作用，它将命令转发给TOP站点。
 * 
 * @author scott.liang
 * @version 1.0 6/30/2018
 * @since laxcus 1.0
 */
public class BankTakeCallItemInvoker extends BankInvoker {

	/**
	 * 构造获取CALL站点成员调用器，指定命令
	 * @param cmd 获取CALL站点成员
	 */
	public BankTakeCallItemInvoker(TakeCallItem cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeCallItem getCommand() {
		return (TakeCallItem) super.getCommand();
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		TakeCallItem cmd = getCommand();
		// 发送给TOP站点
		boolean success = launchToHub(cmd);
		// 不成功，通知GATE站点
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
		TakeCallItemProduct product = null;

		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TakeCallItemProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功，通知GATE站点
		boolean success = (product != null);
		if (success) {
			success = replyProduct(product);
		} else {
			refuse();
		}

		return useful(success);
	}

}