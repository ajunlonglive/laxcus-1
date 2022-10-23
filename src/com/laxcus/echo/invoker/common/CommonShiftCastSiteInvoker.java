/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.echo.invoker.common;

import com.laxcus.command.site.*;
import com.laxcus.log.client.*;
import com.laxcus.site.*;

/**
 * 转发广播注册站点命令调用器。<br><br>
 * 
 * CastSite有三个子级命令：PushSite、DropSite、DestroySite，本处实际发送的是这三个命令。这是一个单向处理，调用器发送完成退出，不需要等待反馈结果。<br><br>
 * 
 * 发送端是TOP/HOM/BANK的管理站点，接收端是TOP/HOME/BANK的监视站点和WATCH站点。<br><br>
 * 
 * 接收端的调用器见：xxxPushSiteInvoker、xxxDropSiteInvoker、xxxDestroySiteInvoker。<br><br>
 * 
 * @author scott.liang
 * @version 1.0 3/21/2013
 * @since laxcus 1.0
 */
public class CommonShiftCastSiteInvoker extends CommonInvoker {

	/**
	 * 构造转发广播注册站点命令调用器，指定命令。
	 * @param shift 转发广播注册站点命令
	 */
	public CommonShiftCastSiteInvoker(ShiftCastSite shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftCastSite getCommand() {
		return (ShiftCastSite) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftCastSite shift = getCommand();
		Node[] sites = shift.getRemotes();

		CastSite cmd = shift.getCommand();

		// 向一批目标站点发送命令
		int count = directTo(sites, cmd);
		boolean success = (count > 0);

		Logger.debug(this, "launch", success, "send count: %d", count);

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