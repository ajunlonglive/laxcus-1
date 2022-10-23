/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import com.laxcus.command.*;

/**
 * @author scott.liang
 *
 */
public class HomeFindAnchorSiteInvoker extends HomeInvoker {

	/**
	 * @param cmd
	 */
	public HomeFindAnchorSiteInvoker(Command cmd) {
		super(cmd);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.pool.echo.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub

		return true;
	}

}
