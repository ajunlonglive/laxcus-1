/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.access.fast.*;

/**
 * 卸载索引调用器。
 * 
 * @author scott.liang
 * @version 1.1 09/03/2012
 * @since laxcus 1.0
 */
public class HomeStopIndexInvoker extends HomeFastMassInvoker {

	/**
	 * 构造卸载索引调用器，指定命令
	 * @param cmd 卸载索引命令
	 */
	public HomeStopIndexInvoker(StopIndex cmd) {
		super(cmd);
	}

	//	/* (non-Javadoc)
	//	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	//	 */
	//	@Override
	//	public boolean launch() {
	//		// TODO Auto-generated method stub
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
