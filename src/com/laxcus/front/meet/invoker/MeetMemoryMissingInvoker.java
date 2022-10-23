/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.missing.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 内存空间不足调用器 <br>
 * 在CONSOLE/TERMINAL界面上显示
 * 
 * @author scott.liang
 * @version 1.0 8/17/2019
 * @since laxcus 1.0
 */
public class MeetMemoryMissingInvoker extends MeetInvoker {

	/**
	 * 构造内存空间不足调用器，指定命令
	 * @param cmd 内存空间不足
	 */
	public MeetMemoryMissingInvoker(MemoryMissing cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public MemoryMissing getCommand() {
		return (MemoryMissing) super.getCommand();
	} 
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		MemoryMissing cmd = getCommand();
		Node site = cmd.getSite();
		// 没有定义，是本地
		if (site == null) {
			site = getLocal();
		}

		String tip = site.toString();
		// 在图形窗口上显示
		warningX(WarningTip.MEMORY_MISSING_X, tip);

		// 退出
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
