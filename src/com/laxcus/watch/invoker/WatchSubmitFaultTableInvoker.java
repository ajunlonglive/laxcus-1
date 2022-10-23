/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import com.laxcus.access.schema.*;
import com.laxcus.command.access.table.*;
import com.laxcus.site.*;
import com.laxcus.util.tip.*;

/**
 * 提交故障表调用器 <br>
 * WATCH站点在图形界面上显示。
 * 
 * @author scott.liang
 * @version 1.0 6/26/2019
 * @since laxcus 1.0
 */
public class WatchSubmitFaultTableInvoker extends WatchInvoker {

	/**
	 * 构造提交故障表调用器，指定命令
	 * @param cmd 提交故障表
	 */
	public WatchSubmitFaultTableInvoker(SubmitFaultTable cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public SubmitFaultTable getCommand() {
		return (SubmitFaultTable) super.getCommand();
	} 
	
	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		SubmitFaultTable cmd = getCommand();
		Node local = cmd.getSite();
		for (FaultTable e : cmd.list()) {
			String tip = String.format("%s | %s # %s", local, e.getSiger(), e.getSpace());
			// 警告
			warningX(WarningTip.DISK_MISSING_X, tip);
		}

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
