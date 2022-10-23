/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.limit.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 删除拒绝操作命令调用器
 * 
 * @author scott.liang
 * @version 3/23/2017
 * @since laxcus 1.0
 */
public class MeetDropLimitInvoker extends MeetLimitInvoker {

	/**
	 * 构造默认的删除拒绝操作命令调用器
	 * @param cmd
	 */
	public MeetDropLimitInvoker(DropLimit cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropLimit getCommand() {
		return (DropLimit) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		int index = findEchoKey(0);
		DropLimitProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropLimitProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}
		
		DropLimit cmd = getCommand();

		// 判断成功或者失败
		boolean success = (product != null);
		if (success) {
			if (product.isEmpty()) { // 空集合发出警告
				warningX(WarningTip.CANNOT_IMPLEMENT_X, cmd.getPrimitive());
			} else {
				print(product.list());
			}
		} else {
			faultX(FaultTip.FAILED_X, cmd);
		}

		return useful(success);
	}

}
