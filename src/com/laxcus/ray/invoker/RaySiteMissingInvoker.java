/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.ray.invoker;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 站点不足通知调用器
 * 
 * @author scott.liang
 * @version 1.0 6/8/2018
 * @since laxcus 1.0
 */
public class RaySiteMissingInvoker extends RayInvoker {

	/**
	 * 构造站点不足通知调用器，指定命令
	 * @param cmd 站点不足通知
	 */
	public RaySiteMissingInvoker(SiteMissing cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SiteMissing getCommand() {
		return (SiteMissing) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SiteMissing cmd = getCommand();

		// 单元
		for (SiteMissingItem item : cmd.list()) {
			warningX(WarningTip.USERSITE_MSSING_X, item.getSiger(), SiteTag.translate(item.getSiteFamily()));
		}
		
		// 加声音提示！

		return useful();
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
