/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.front.meet.invoker;

import com.laxcus.command.missing.*;
import com.laxcus.util.tip.*;

/**
 * 磁盘空间不足调用器 <br>
 * 在CONSOLE/TERMINAL界面上显示
 * 
 * @author scott.liang
 * @version 1.0 8/18/2019
 * @since laxcus 1.0
 */
public class MeetDiskMissingInvoker extends MeetInvoker {

	/**
	 * 构造磁盘空间不足调用器，指定命令
	 * @param cmd 磁盘空间不足
	 */
	public MeetDiskMissingInvoker(DiskMissing cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public DiskMissing getCommand() {
		return (DiskMissing) super.getCommand();
	} 
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		DiskMissing cmd = getCommand();
		// 没有定义，是本地
		if (cmd.getSite() == null) {
			cmd.setSite(getLocal());
		}

		String tip = cmd.toString();
		// 在图形窗口上显示
		warningX(WarningTip.DISK_MISSING_X, tip);

		// 退出
		return useful(true);
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
