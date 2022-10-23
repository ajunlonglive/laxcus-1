/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.access.schema.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 显示数据库命令的异步调用器。
 * 
 * @author scott.liang
 * @version 1.0 7/23/2013
 * @since laxcus 1.0
 */
public class DriverShowSchemaInvoker extends DriverInvoker {

	/**
	 * 构造显示数据库命令的异步调用器
	 * @param mission 驱动任务
	 */
	public DriverShowSchemaInvoker(DriverMission mission) {
		super(mission);
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
			return false;
		}

		// 判断成功
		boolean success = (product != null);
		if (success) {
			setProduct(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		return useful(success);
	}

}
