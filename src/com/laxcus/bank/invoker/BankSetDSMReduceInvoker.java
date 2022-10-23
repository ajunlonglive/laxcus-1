/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.bank.invoker;

import com.laxcus.command.rebuild.*;
import com.laxcus.echo.*;
import com.laxcus.log.client.*;
import com.laxcus.visit.*;

/**
 * BANK站点的设置DSM表压缩倍数异步调用器。<br>
 * 命令来自GATE站点，BANK在此起中继作用，将命令转发给TOP站点，然后把TOP的反馈给GATE站点。
 * 
 * @author scott.liang
 * @version 1.0 6/2/2019
 * @since laxcus 1.0
 */
public class BankSetDSMReduceInvoker extends BankInvoker {

	/**
	 * 构造设置DSM表压缩倍数异步调用器，指定命令
	 * @param cmd SetDSMReduce命令
	 */
	public BankSetDSMReduceInvoker(SetDSMReduce cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SetDSMReduce getCommand() {
		return (SetDSMReduce) super.getCommand();
	}
	
	/**
	 * 失败！
	 * @return
	 */
	private boolean fault() {
		return replyFault(Major.FAULTED, Minor.IMPLEMENT_FAILED);
	}
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 不用考虑，直接投递给TOP站点
		boolean success = launchToHub();
		if (!success) {
			fault();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		SetDSMReduceProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SetDSMReduceProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		// 判断成功
		boolean success = (product != null);
		if (success) {
			replyProduct(product);
		} else {
			fault();
		}

		return useful(success);
	}

}
