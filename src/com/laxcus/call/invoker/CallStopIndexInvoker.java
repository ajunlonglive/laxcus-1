/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.call.invoker;

import com.laxcus.command.access.fast.*;

/**
 * “卸载索引”命令调用器。
 * 
 * @author scott.liang
 * @version 1.1 8/2/2012
 * @since laxcus 1.0
 */
public class CallStopIndexInvoker extends CallFastMassInvoker {

	/**
	 * 构造“卸载索引”命令调用器
	 * @param cmd
	 */
	public CallStopIndexInvoker(StopIndex cmd) {
		super(cmd);
	}

//	/* (non-Javadoc)
//	 * @see com.laxcus.top.runner.EchoRunner#launch()
//	 */
//	@Override
//	public boolean launch() {
//		
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
//	 */
//	@Override
//	public boolean ending() {
//		// TODO Auto-generated method stub
//
//		return true;
//	}

}
