/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.top.invoker;

import com.laxcus.command.account.*;
import com.laxcus.site.*;
import com.laxcus.top.pool.*;

/**
 * 获得组件站点调用器。<br>
 * TOP站点在此起中继作用。它找到BANK管理站点，将命令转发给它。
 * 
 * @author scott.liang
 * @version 1.1 8/2/2014
 * @since laxcus 1.0
 */
public class TopTakeSigerSiteInvoker extends TopInvoker {

	/**
	 * 构造获得组件站点调用器，指定命令
	 * @param cmd 获得组件站点
	 */
	public TopTakeSigerSiteInvoker(TakeSigerSite cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public TakeSigerSite getCommand() {
		return (TakeSigerSite) super.getCommand();
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		// 找到BANK主站点
		Node slave = BankOnTopPool.getInstance().getManagerSite();

		boolean success = (slave != null);
		if (success) {
			TakeSigerSite cmd = getCommand();
			success = launchTo(slave, cmd);
		}
		if (!success) {
			failed();
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#ending()
	 */
	@Override
	public boolean ending() {
		// 反馈结果
		return reflect();
	}

}
