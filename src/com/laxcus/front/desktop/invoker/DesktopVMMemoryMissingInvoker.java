/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.desktop.invoker;

import com.laxcus.command.missing.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 虚拟机内存空间不足调用器 <br>
 * 在CONSOLE/TERMINAL界面上显示
 * 
 * @author scott.liang
 * @version 1.0 5/30/2021
 * @since laxcus 1.0
 */
public class DesktopVMMemoryMissingInvoker extends DesktopInvoker {

	/**
	 * 构造虚拟机内存空间不足调用器，指定命令
	 * @param cmd 虚拟机内存空间不足
	 */
	public DesktopVMMemoryMissingInvoker(VMMemoryMissing cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public VMMemoryMissing getCommand() {
		return (VMMemoryMissing) super.getCommand();
	} 
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		VMMemoryMissing cmd = getCommand();
		Node site = cmd.getSite();
		// 没有定义，是本地
		if (site == null) {
			site = getLocal();
		}

		String tip = site.toString();
		// 在图形窗口上显示
		warningX(WarningTip.VMMEMORY_MISSING_X, tip);

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
