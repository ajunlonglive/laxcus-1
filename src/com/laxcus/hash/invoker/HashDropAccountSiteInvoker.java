/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.hash.invoker;

import com.laxcus.command.site.bank.*;
import com.laxcus.hash.pool.*;
import com.laxcus.log.client.*;

/**
 * 撤销ACCOUNT站点到HASH站点调用器
 * 
 * @author scott.liang
 * @version 1.0 9/23/2018
 * @since laxcus 1.0
 */
public class HashDropAccountSiteInvoker extends HashInvoker {

	/**
	 * 构造撤销ACCOUNT站点到HASH站点，指定命令
	 * @param cmd 撤销ACCOUNT站点到HASH站点
	 */
	public HashDropAccountSiteInvoker(DropAccountSite cmd) {
		super(cmd);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DropAccountSite getCommand() {
		return (DropAccountSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DropAccountSite cmd = getCommand();
		boolean success = StaffOnHashPool.getInstance().remove(cmd.getNode());

		Logger.debug(this, "launch", success, "remove %s", cmd.getNode());

		return useful(success);
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// TODO Auto-generated method stub
		return false;
	}

}