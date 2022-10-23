/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 laxcus.com. All rights reserved
 * 
 * @license Laxcus Public License (LPL)
 */
package com.laxcus.watch.invoker;

import java.awt.Color;

import com.laxcus.command.site.watch.*;
import com.laxcus.util.display.show.*;
import com.laxcus.watch.pool.*;

/**
 * 被WATCH监视节点的定时刷新调用器
 * 
 * @author scott.liang
 * @version 1.0 4/13/2018
 * @since laxcus 1.0
 */
public class WatchOutlookIntervalInvoker extends WatchInvoker {

	/**
	 * 构造被WATCH监视节点的定时刷新调用器，指定命令
	 * @param cmd 被WATCH监视节点的定时刷新命令
	 */
	public WatchOutlookIntervalInvoker(OutlookInterval cmd) {
		super(cmd);
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#isDistributed()
	 */
	@Override
	public boolean isDistributed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#getCommand()
	 */
	@Override
	public OutlookInterval getCommand() {
		return (OutlookInterval) super.getCommand();
	}

	/* (non-Javadoc)
	 * @see com.laxcus.echo.invoke.EchoInvoker#launch()
	 */
	@Override
	public boolean launch() {
		OutlookInterval cmd = getCommand();

		// 更新超时时间
		WatchTube.setTimeout(cmd.getInterval());

		print(WatchTube.getTimeout());

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
	
	/**
	 * 打印时间
	 * @param ms
	 */
	private void print(long ms) {
		createShowTitle(new String[] { "OUTLOOK-INTERVAL/TIME" });

		String text = doStyleTime(ms);
		
		// 提取前景色
		Color foreground = findXMLForeground("OUTLOOK-INTERVAL/TIME");

		ShowItem item = new ShowItem();
		item.add(new ShowStringCell(0, text, foreground));
		addShowItem(item);
		
		// 输出全部表
		flushTable();
	}

}