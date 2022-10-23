/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.driver.invoker;

import com.laxcus.command.rebuild.*;
import com.laxcus.front.driver.mission.*;
import com.laxcus.log.client.*;
import com.laxcus.util.tip.*;
import com.laxcus.visit.*;

/**
 * 撤销数据表优化时间命令调用器。<br>
 * 
 * @author scott.liang
 * @version 1.1 9/2/2012
 * @since laxcus 1.0
 */
public class DriverDropRegulateTimeInvoker extends DriverInvoker {

	/**
	 * 构造撤销数据表优化时间命令调用器
	 * @param mission 驱动任务
	 */
	public DriverDropRegulateTimeInvoker(DriverMission mission) {
		super(mission);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropRegulateTime getCommand() {
		return (DropRegulateTime) super.getCommand();
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
		int index = findEchoKey(0);
		DropRegulateTimeProduct product = null;
		try {
			if (isSuccessObjectable(index)) {
				product = getObject(DropRegulateTimeProduct.class, index);
			}
		} catch (VisitException e) {
			Logger.error(e);
		}

		boolean success = (product != null && product.isSuccessful());
		if (success) {
			setProduct(product);
		} else {
			faultX(FaultTip.FAILED_X, getCommand());
		}

		return useful(success);
	}


}