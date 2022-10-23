/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.command.missing.*;
import com.laxcus.util.tip.*;
import com.laxcus.site.*;

/**
 * 磁盘空间不足调用器 <br>
 * WATCH站点在图形界面上显示，同时用声音报警。
 * 
 * @author scott.liang
 * @version 1.0 3/18/2017
 * @since laxcus 1.0
 */
public class WatchDiskMissingInvoker extends WatchInvoker {

	/**
	 * 构造磁盘空间不足调用器，指定命令
	 * @param cmd 磁盘空间不足
	 */
	public WatchDiskMissingInvoker(DiskMissing cmd) {
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
		Node site = cmd.getSite();
		// 没有定义节点时，是本地产生
		if (site == null) {
			site = getLocal();
		}

		// 判断被拒绝出现
		boolean refuse = isRefuseWarning(site);
		// 不是拒绝，显示它
		if (!refuse) {
			String tip = cmd.toString();
			// 在图形窗口上显示
			warningX(WarningTip.DISK_MISSING_X, tip);
		}

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
