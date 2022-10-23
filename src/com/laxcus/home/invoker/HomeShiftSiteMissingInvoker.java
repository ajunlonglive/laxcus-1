/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.home.invoker;

import java.util.*;

import com.laxcus.command.site.watch.*;
import com.laxcus.site.*;

/**
 * 转发站点不足命令调用器
 * 
 * @author scott.liang
 * @version 1.0 6/8/2018
 * @since laxcus 1.0
 */
public class HomeShiftSiteMissingInvoker extends HomeInvoker {

	/**
	 * 构造转发站点不足命令调用器，指定转发命令
	 * @param shift 转发站点不足命令
	 */
	public HomeShiftSiteMissingInvoker(ShiftSiteMissing shift) {
		super(shift);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public ShiftSiteMissing getCommand() {
		return (ShiftSiteMissing) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		ShiftSiteMissing shift = getCommand();
		List<Node> sites = shift.getSites();
		SiteMissing cmd = shift.getCommand();

		// 以容错模式发送到WATCH站点
		int count = directTo(sites, cmd);
		boolean success = (count > 0);

		// 退出
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
