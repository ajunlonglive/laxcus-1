/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.access.schema.*;
import com.laxcus.visit.*;

/**
 * 显示数据库命令的异步调用器。
 * 
 * @author scott.liang
 * @version 1.2 7/23/2013
 * @since laxcus 1.0
 */
public class MeetShowSchemaInvoker extends MeetShowReferenceInvoker {

	/**
	 * 构造显示数据库命令的异步调用器，指定命令
	 * @param cmd 显示数据库命令
	 */
	public MeetShowSchemaInvoker(ShowSchema cmd) {
		super(cmd);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		return fireToHub();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.terminal.invoker.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		SchemaProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(SchemaProduct.class, index);
			}
		} catch (VisitException e) {
			fault(e);
		}

		// 判断成功
		boolean success = (product != null);
		if (success) {
			printSchemas(product.list());
		} else {
			printFault();
		}

		return useful(success);
	}

}
