/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.access.table.*;
import com.laxcus.visit.*;

/**
 * 显示数据表命令的异步调用器。
 * 
 * @author scott.liang
 * @version 1.1 5/29/2021
 * @since laxcus 1.0
 */
public class DesktopShowTableInvoker extends DesktopShowReferenceInvoker {

	/**
	 * 构造显示数据表命令的异步调用器，指定命令
	 * @param cmd 显示数据表命令
	 */
	public DesktopShowTableInvoker(ShowTable cmd) {
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
		TableProduct product = null;
		int index = findEchoKey(0);
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(TableProduct.class, index);
			}
		} catch (VisitException e) {
			super.fault(e);
		}

		// 判断成功
		boolean success = (product != null);
		if (success) {
			printTables(product.list());
		} else {
			printFault();
		}

		return useful(success);
	}

}